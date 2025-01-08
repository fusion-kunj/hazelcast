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

package com.hazelcast.map.impl.querycache.accumulator;

import com.hazelcast.map.impl.querycache.accumulator.BasicAccumulator.ReadOnlyIterator;
import com.hazelcast.map.impl.querycache.event.DefaultQueryCacheEventData;
import com.hazelcast.map.impl.querycache.event.sequence.Sequenced;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.annotation.ParallelJVMTest;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(HazelcastParallelClassRunner.class)
@Category({QuickTest.class, ParallelJVMTest.class})
public class ReadOnlyIteratorTest {

    private final Sequenced sequenced = new DefaultQueryCacheEventData();

    private ReadOnlyIterator<Sequenced> iterator;

    @Before
    public void setUp() {
        CyclicBuffer<Sequenced> buffer = new DefaultCyclicBuffer<>(1);
        sequenced.setSequence(1);
        buffer.add(sequenced);

        iterator = new ReadOnlyIterator<>(buffer);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructor_whenBufferIsNull_thenThrowException() {
        iterator = new ReadOnlyIterator<>(null);
    }

    @Test
    public void testIteration() {
        assertTrue(iterator.hasNext());
        assertEquals(sequenced, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemove() {
        iterator.remove();
    }
}
