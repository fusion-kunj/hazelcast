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

package com.hazelcast.map.impl.mapstore;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.map.MapLoader;
import com.hazelcast.spi.properties.ClusterProperty;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.TestHazelcastInstanceFactory;
import com.hazelcast.test.annotation.ParallelJVMTest;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.util.function.Supplier;

import static com.hazelcast.config.MapStoreConfig.InitialLoadMode.EAGER;
import static com.hazelcast.config.MapStoreConfig.InitialLoadMode.LAZY;
import static org.junit.Assert.assertEquals;

@RunWith(HazelcastParallelClassRunner.class)
@Category({QuickTest.class, ParallelJVMTest.class})
public class MapLoaderMultiNodeTest extends HazelcastTestSupport {

    private static final int MAP_STORE_ENTRY_COUNT = 10000;
    private static final int BATCH_SIZE = 100;
    private static final int NODE_COUNT = 3;

    private final String mapName = "default";

    private TestHazelcastInstanceFactory nodeFactory;
    private CountingMapLoader mapLoader;

    @Before
    public void setUp() {
        nodeFactory = createHazelcastInstanceFactory(NODE_COUNT + 2);
        mapLoader = new CountingMapLoader(MAP_STORE_ENTRY_COUNT);
    }

    @Test
    public void testLoads_whenMapLazyAndCheckingSize() {
        IMap<Object, Object> map = getMap(mapName, () -> newConfig(mapName, LAZY));

        assertSizeAndLoadCount(map);
    }

    @Test
    public void testLoadsAll_whenMapCreatedInEager() {
        IMap<Object, Object> map = getMap(mapName, () -> newConfig(mapName, EAGER));

        assertSizeAndLoadCount(map);
    }

    @Test
    public void testLoadsNothing_whenMapCreatedLazy() {
        getMap(mapName, () -> newConfig(mapName, LAZY));

        assertEquals(0, mapLoader.getLoadedValueCount());
    }

    @Test
    public void testLoadsMap_whenLazyAndValueRetrieved() {
        IMap<Object, Object> map = getMap(mapName, () -> newConfig(mapName, LAZY));

        assertEquals(1, map.get(1));
        assertSizeAndLoadCount(map);
    }

    @Test
    public void testLoadsAll_whenLazyModeAndLoadAll() {
        IMap<Object, Object> map = getMap(mapName, () -> newConfig(mapName, LAZY));
        map.loadAll(true);

        assertEquals(1, mapLoader.getLoadAllKeysInvocations());
        assertSizeAndLoadCount(map);
    }

    @Test
    public void testDoesNotLoadAgain_whenLoadedAndNodeAdded() {
        IMap<Object, Object> map = getMap(mapName, () -> newConfig(mapName, EAGER));
        nodeFactory.newHazelcastInstance(newConfig(mapName, EAGER));

        assertEquals(1, mapLoader.getLoadAllKeysInvocations());
        assertSizeAndLoadCount(map);
    }

    @Test
    public void testDoesNotLoadAgain_whenLoadedLazyAndNodeAdded() {
        IMap<Object, Object> map = getMap(mapName, () -> newConfig(mapName, LAZY));
        map.loadAll(true);
        nodeFactory.newHazelcastInstance(newConfig(mapName, LAZY));

        assertEquals(1, mapLoader.getLoadAllKeysInvocations());
        assertSizeAndLoadCount(map);
    }

    @Test
    public void testLoadAgain_whenLoadedAllCalledMultipleTimes() {
        IMap<Object, Object> map = getMap(mapName, () -> newConfig(mapName, LAZY));
        map.loadAll(true);
        map.loadAll(true);

        assertEquals(2, mapLoader.getLoadAllKeysInvocations());
        assertSizeEventually(MAP_STORE_ENTRY_COUNT, map);
        assertEquals(2 * MAP_STORE_ENTRY_COUNT, mapLoader.getLoadedValueCount());
    }

    @Test
    public void testLoadsOnce_whenSizeCheckedTwice() {
        mapLoader = new CountingMapLoader(MAP_STORE_ENTRY_COUNT, true);
        IMap<Object, Object> map = getMap(mapName, () -> newConfig(mapName, LAZY));
        map.size();
        map.size();

        assertEquals(1, mapLoader.getLoadAllKeysInvocations());
        assertSizeAndLoadCount(map);
    }

    protected void assertSizeAndLoadCount(IMap<Object, Object> map) {
        assertSizeEventually(MAP_STORE_ENTRY_COUNT, map);
        assertEquals(MAP_STORE_ENTRY_COUNT, mapLoader.getLoadedValueCount());
    }

    protected IMap<Object, Object> getMap(final String mapName, Supplier<Config> cfgSupplier) {
        HazelcastInstance hz = nodeFactory.newInstances(cfgSupplier, NODE_COUNT)[0];
        assertClusterSizeEventually(NODE_COUNT, hz);
        IMap<Object, Object> map = hz.getMap(mapName);
        waitClusterForSafeState(hz);
        return map;
    }

    protected Config newConfig(String mapName, MapStoreConfig.InitialLoadMode loadMode) {
        return newConfig(mapName, loadMode, 1, mapLoader);
    }

    protected Config newConfig(String mapName, MapStoreConfig.InitialLoadMode loadMode, int backups, MapLoader loader) {
        Config cfg = getConfig();
        cfg.setClusterName(getClass().getSimpleName());
        cfg.setProperty(ClusterProperty.MAP_LOAD_CHUNK_SIZE.getName(), Integer.toString(BATCH_SIZE));
        cfg.setProperty(ClusterProperty.PARTITION_COUNT.getName(), "31");

        MapStoreConfig mapStoreConfig = new MapStoreConfig().setImplementation(loader).setInitialLoadMode(loadMode);

        cfg.getMapConfig(mapName).setMapStoreConfig(mapStoreConfig).setBackupCount(backups);

        return cfg;
    }
}
