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

package com.hazelcast.jet.sql.impl.connector.kafka;

import com.hazelcast.dataconnection.DataConnection;
import com.hazelcast.jet.kafka.KafkaDataConnection;
import com.hazelcast.test.HazelcastSerialClassRunner;
import com.hazelcast.test.annotation.NightlyTest;
import com.hazelcast.test.annotation.ParallelJVMTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static com.hazelcast.test.Accessors.getNodeEngineImpl;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(HazelcastSerialClassRunner.class)
@Category({NightlyTest.class, ParallelJVMTest.class})
public class KafkaDataConnectionSqlTest extends KafkaSqlTestSupport {

    @Test
    public void when_createSharedDataConnection_then_success() {
        String dlName = randomName();
        createSqlKafkaDataConnection(dlName, true);
        var dataConnectionService = getNodeEngineImpl(instance()).getDataConnectionService();
        DataConnection dataConnection = dataConnectionService.getAndRetainDataConnection(dlName, KafkaDataConnection.class);

        assertThat(dataConnection).isNotNull();
        assertThat(dataConnection.getConfig().getType()).isEqualTo("Kafka");
        assertThat(dataConnection.getConfig().isShared()).isTrue();
    }

    @Test
    public void when_createNonSharedDataConnection_then_success() {
        String dlName = randomName();
        createSqlKafkaDataConnection(dlName, false);

        var dataConnectionService = getNodeEngineImpl(instance()).getDataConnectionService();
        DataConnection dataConnection = dataConnectionService.getAndRetainDataConnection(dlName, KafkaDataConnection.class);

        assertThat(dataConnection).isNotNull();
        assertThat(dataConnection.getConfig().getType()).isEqualTo("Kafka");
        assertThat(dataConnection.getConfig().isShared()).isFalse();
    }
}
