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

package com.hazelcast.spring.cache;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.instance.impl.Node;
import com.hazelcast.internal.services.NodeAware;
import com.hazelcast.spring.context.SpringAware;
import org.springframework.beans.factory.annotation.Autowired;

import javax.cache.configuration.Factory;
import javax.cache.expiry.ExpiryPolicy;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@SpringAware
public class JCacheExpiryPolicyFactory implements Factory<ExpiryPolicy>, HazelcastInstanceAware, NodeAware {

    public static final AtomicBoolean HAZELCAST_INSTANCE_INJECTED = new AtomicBoolean();
    public static final AtomicBoolean NODE_INJECTED = new AtomicBoolean();
    public static final AtomicReference<IJCacheDummyBean> INJECTED_DUMMY_BEAN = new AtomicReference<>();

    private IJCacheDummyBean dummyBean;

    public JCacheExpiryPolicyFactory() {
    }

    @Override
    public ExpiryPolicy create() {
        return new JCacheExpiryPolicy();
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        HAZELCAST_INSTANCE_INJECTED.set(true);
    }

    @Override
    public void setNode(Node node) {
        NODE_INJECTED.set(true);
    }

    public IJCacheDummyBean getDummyBean() {
        return dummyBean;
    }

    @Autowired
    public void setDummyBean(IJCacheDummyBean dummyBean) {
        INJECTED_DUMMY_BEAN.set(dummyBean);
        this.dummyBean = dummyBean;
    }
}
