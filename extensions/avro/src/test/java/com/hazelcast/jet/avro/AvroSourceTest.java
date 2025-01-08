/*
 * Copyright 2025 Hazelcast Inc.
 *
 * Licensed under the Hazelcast Community License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://hazelcast.com/hazelcast-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.jet.avro;

import com.hazelcast.collection.IList;
import com.hazelcast.internal.nio.IOUtil;
import com.hazelcast.jet.SimpleTestInClusterSupport;
import com.hazelcast.jet.avro.generated.SpecificUser;
import com.hazelcast.jet.avro.model.User;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.test.annotation.ParallelJVMTest;
import com.hazelcast.test.annotation.QuickTest;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.apache.avro.specific.SpecificDatumWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.hazelcast.jet.avro.AvroSources.AVRO_SOURCE_CONNECTOR_NAME;
import static com.hazelcast.jet.pipeline.JetPhoneHomeTestUtil.assertConnectorPhoneHomeCollected;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category({QuickTest.class, ParallelJVMTest.class})
public class AvroSourceTest extends SimpleTestInClusterSupport {
    private static final int TOTAL_RECORD_COUNT = 20;

    private File directory;
    private IList<?> list;

    @BeforeClass
    public static void setup() {
        initializeWithClient(2, null, null);
    }

    @Before
    public void createDirectory() throws Exception {
        directory = createTempDirectory();

        list = client().getList(randomName());
    }

    @After
    public void cleanup() {
        IOUtil.delete(directory);
    }

    @Test
    public void testReflectReader() {
        Pipeline pipeline = getReflectReaderPipelineSupplier().get();

        instance().getJet().newJob(pipeline).join();

        assertEquals(TOTAL_RECORD_COUNT, list.size());
        assertTrue(list.contains(new User("name-1", 1)));
    }

    @Test
    public void testSpecificReader() {
        Pipeline pipeline = getSpecificReaderPipeline().get();

        instance().getJet().newJob(pipeline).join();

        assertEquals(TOTAL_RECORD_COUNT, list.size());
        assertTrue(list.contains(new SpecificUser("name-1", 1)));
    }

    @Test
    public void testGenericReader() {
        Pipeline pipeline = getGenericReaderPipeline().get();

        instance().getJet().newJob(pipeline).join();

        assertEquals(TOTAL_RECORD_COUNT, list.size());
        assertTrue(list.contains(new User("name-1", 1)));
    }

    @Test
    public void testPhoneHome() {
        List<Supplier<Pipeline>> pipelineSuppliers = getPipelineSetupFunctions();

        assertConnectorPhoneHomeCollected(pipelineSuppliers, AVRO_SOURCE_CONNECTOR_NAME, (unused) -> {
            cleanup();
            try {
                createDirectory();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        }, true, instance());
    }

    private Supplier<Pipeline> getReflectReaderPipelineSupplier() {
        return () -> {
            try {
                createAvroFiles(new ReflectDatumWriter<>(User.class), User.classSchema(), i -> new User("name-" + i, i));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Pipeline pipeline = Pipeline.create();
            pipeline.readFrom(AvroSources.files(directory.getPath(), User.class))
                    .distinct()
                    .writeTo(Sinks.list(list.getName()));
            return pipeline;
        };
    }

    private Supplier<Pipeline> getSpecificReaderPipeline() {
        return () -> {
            try {
                createAvroFiles(new SpecificDatumWriter<>(SpecificUser.class), SpecificUser.getClassSchema(),
                        i -> new SpecificUser("name-" + i, i));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Pipeline pipeline = Pipeline.create();
            pipeline.readFrom(AvroSources.files(directory.getPath(), SpecificUser.class))
                    .distinct()
                    .writeTo(Sinks.list(list.getName()));
            return pipeline;
        };
    }

    private Supplier<Pipeline> getGenericReaderPipeline() {
        return () -> {
            try {
                createAvroFiles(new GenericDatumWriter<>(), User.classSchema(), AvroSourceTest::record);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Pipeline pipeline = Pipeline.create();
            pipeline.readFrom(AvroSources.files(directory.getPath(), (file, record) -> toUser(record)))
                    .distinct()
                    .writeTo(Sinks.list(list.getName()));
            return pipeline;
        };

    }

    private List<Supplier<Pipeline>> getPipelineSetupFunctions() {
        List<Supplier<Pipeline>> pipelineSetupFunctions = new ArrayList<>();
        pipelineSetupFunctions.add(getReflectReaderPipelineSupplier());
        pipelineSetupFunctions.add(getSpecificReaderPipeline());
        pipelineSetupFunctions.add(getGenericReaderPipeline());
        return pipelineSetupFunctions;
    }

    private <R> void createAvroFiles(DatumWriter<R> datumWriter, Schema schema, Function<Integer, R> datumFn)
            throws IOException {
        try (DataFileWriter<R> writer = new DataFileWriter<>(datumWriter)) {
            writer.create(schema, new File(directory, randomString()));
            for (int i = 0; i < TOTAL_RECORD_COUNT; i++) {
                writer.append(datumFn.apply(i));
            }
        }
    }

    private static GenericRecord record(int i) {
        Schema schema = ReflectData.get().getSchema(User.class);
        GenericRecord record = (GenericRecord) GenericData.get().newRecord(null, schema);
        record.put("name", "name-" + i);
        record.put("favoriteNumber", i);
        return record;
    }

    private static User toUser(GenericRecord record) {
        return new User(record.get("name").toString(), Integer.parseInt(record.get("favoriteNumber").toString()));
    }
}
