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

package com.hazelcast.client.impl.protocol.task.dynamicconfig;

import com.hazelcast.client.impl.protocol.ClientMessage;
import com.hazelcast.client.impl.protocol.codec.DynamicConfigAddCacheConfigCodec;
import com.hazelcast.config.CacheConfig;
import com.hazelcast.config.CacheSimpleConfig;
import com.hazelcast.config.DataPersistenceConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class AddCacheConfigMessageTaskTest extends ConfigMessageTaskTest<AddCacheConfigMessageTask> {
    @Test
    public void doNotThrowException_whenNullValuesProvidedForNullableFields() throws Exception {
        CacheConfig<Object, Object> cacheConfig = new CacheConfig<>("my-cache");
        ClientMessage addMapConfigClientMessage = DynamicConfigAddCacheConfigCodec.encodeRequest(
                cacheConfig.getName(),
                null,
                null,
                cacheConfig.isStatisticsEnabled(),
                cacheConfig.isManagementEnabled(),
                cacheConfig.isReadThrough(),
                cacheConfig.isWriteThrough(),
                null,
                null,
                null,
                null,
                cacheConfig.getBackupCount(),
                cacheConfig.getAsyncBackupCount(),
                cacheConfig.getInMemoryFormat().name(),
                null,
                null,
                0,
                cacheConfig.isDisablePerEntryInvalidationEvents(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                cacheConfig.getDataPersistenceConfig(),
                cacheConfig.getUserCodeNamespace()
        );
        AddCacheConfigMessageTask addCacheConfigMessageTask = createMessageTask(addMapConfigClientMessage);
        addCacheConfigMessageTask.run();
        CacheConfig<Object, Object> transmittedCacheConfig = new CacheConfig<>((CacheSimpleConfig) addCacheConfigMessageTask.getConfig());
        assertEquals(cacheConfig, transmittedCacheConfig);
    }

    @Test
    public void testDataPersistenceSubConfigTransmittedCorrectly() throws Exception {
        CacheConfig<Object, Object> cacheConfig = new CacheConfig<>("my-cache");
        DataPersistenceConfig dataPersistenceConfig = new DataPersistenceConfig();
        dataPersistenceConfig.setEnabled(true);
        dataPersistenceConfig.setFsync(true);
        cacheConfig.setDataPersistenceConfig(dataPersistenceConfig);
        ClientMessage addMapConfigClientMessage = DynamicConfigAddCacheConfigCodec.encodeRequest(
                cacheConfig.getName(),
                null,
                null,
                cacheConfig.isStatisticsEnabled(),
                cacheConfig.isManagementEnabled(),
                cacheConfig.isReadThrough(),
                cacheConfig.isWriteThrough(),
                null,
                null,
                null,
                null,
                cacheConfig.getBackupCount(),
                cacheConfig.getAsyncBackupCount(),
                cacheConfig.getInMemoryFormat().name(),
                null,
                null,
                0,
                cacheConfig.isDisablePerEntryInvalidationEvents(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                cacheConfig.getDataPersistenceConfig(),
                cacheConfig.getUserCodeNamespace()
        );
        AddCacheConfigMessageTask addCacheConfigMessageTask = createMessageTask(addMapConfigClientMessage);
        addCacheConfigMessageTask.run();
        CacheConfig<Object, Object> transmittedCacheConfig = new CacheConfig<>((CacheSimpleConfig) addCacheConfigMessageTask.getConfig());
        assertEquals(cacheConfig, transmittedCacheConfig);
    }

    @Override
    protected AddCacheConfigMessageTask createMessageTask(ClientMessage clientMessage) {
        return new AddCacheConfigMessageTask(clientMessage, logger, mockNodeEngine, mock(), mockClientEngine, mockConnection,
                mockNodeExtension, mock(), config, mock());
    }
}
