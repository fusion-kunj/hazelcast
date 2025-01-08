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

package com.hazelcast.internal.crdt.pncounter;

import com.hazelcast.crdt.pncounter.PNCounter;
import com.hazelcast.partition.NoDataMemberInClusterException;
import com.hazelcast.test.HazelcastTestSupport;
import org.junit.Test;

/**
 * Base test for testing behaviour of PN counter behaviour when there are no data members in the cluster
 */
public abstract class AbstractPNCounterNoDataMemberTest extends HazelcastTestSupport {

    @Test(expected = NoDataMemberInClusterException.class)
    public void noDataMemberExceptionIsThrown() {
        final PNCounter driver = getCounter();
        mutate(driver);
    }

    protected abstract void mutate(PNCounter driver);

    protected abstract PNCounter getCounter();
}
