/*
 * Copyright (c) 2008-2025, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.splitbrainprotection.ringbuffer;

import com.hazelcast.ringbuffer.OverflowPolicy;
import com.hazelcast.ringbuffer.Ringbuffer;
import com.hazelcast.splitbrainprotection.AbstractSplitBrainProtectionTest;
import com.hazelcast.splitbrainprotection.SplitBrainProtectionException;
import com.hazelcast.splitbrainprotection.SplitBrainProtectionOn;
import com.hazelcast.test.HazelcastParametrizedRunner;
import com.hazelcast.test.HazelcastSerialParametersRunnerFactory;
import com.hazelcast.test.TestHazelcastInstanceFactory;
import com.hazelcast.test.annotation.ParallelJVMTest;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Parameterized.UseParametersRunnerFactory;

import static com.hazelcast.splitbrainprotection.SplitBrainProtectionOn.READ_WRITE;
import static com.hazelcast.splitbrainprotection.SplitBrainProtectionOn.WRITE;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("unchecked")
@RunWith(HazelcastParametrizedRunner.class)
@UseParametersRunnerFactory(HazelcastSerialParametersRunnerFactory.class)
@Category({QuickTest.class, ParallelJVMTest.class})
public class RingbufferSplitBrainProtectionWriteTest extends AbstractSplitBrainProtectionTest {

    @Parameters(name = "splitBrainProtectionType:{0}")
    public static Iterable<Object[]> parameters() {
        return asList(new Object[][]{{WRITE}, {READ_WRITE}});
    }

    @Parameter
    public static SplitBrainProtectionOn splitBrainProtectionOn;

    @BeforeClass
    public static void setUp() {
        initTestEnvironment(smallInstanceConfig(), new TestHazelcastInstanceFactory());
    }

    @AfterClass
    public static void tearDown() {
        shutdownTestEnvironment();
    }

    @Test
    public void add_splitBrainProtection() {
        ring(0).add("123");
    }

    @Test(expected = SplitBrainProtectionException.class)
    public void add_noSplitBrainProtection() {
        ring(3).add("123");
    }

    @Test
    public void addAllAsync_splitBrainProtection() throws Exception {
        ring(0).addAllAsync(singletonList("123"), OverflowPolicy.OVERWRITE).toCompletableFuture().get();
    }

    @Test
    public void addAllAsync_noSplitBrainProtection() {
        assertThatThrownBy(() -> ring(3).addAllAsync(singletonList("123"), OverflowPolicy.OVERWRITE).toCompletableFuture().get())
                .hasCauseInstanceOf(SplitBrainProtectionException.class);
    }

    @Test
    public void addAsync_splitBrainProtection() throws Exception {
        ring(0).addAsync("123", OverflowPolicy.OVERWRITE).toCompletableFuture().get();
    }

    @Test
    public void addAsync_noSplitBrainProtection() {
        assertThatThrownBy(() ->
                ring(3).addAsync("123", OverflowPolicy.OVERWRITE).toCompletableFuture().get()
        )
                .hasCauseInstanceOf(SplitBrainProtectionException.class);
    }

    protected Ringbuffer ring(int index) {
        return ring(index, splitBrainProtectionOn);
    }
}
