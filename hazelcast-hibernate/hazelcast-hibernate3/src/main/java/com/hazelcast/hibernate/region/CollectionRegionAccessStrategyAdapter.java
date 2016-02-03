/*
 * Copyright (c) 2008-2016, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.hibernate.region;

import com.hazelcast.hibernate.access.AccessDelegate;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.CollectionRegion;
import org.hibernate.cache.access.CollectionRegionAccessStrategy;
import org.hibernate.cache.access.SoftLock;

/**
 * Simple adapter implementation for transactional / concurrent access control on collections
 *
 * @author Leo Kim (lkim@limewire.com)
 */
public final class CollectionRegionAccessStrategyAdapter implements CollectionRegionAccessStrategy {

    private final AccessDelegate<? extends HazelcastCollectionRegion> delegate;

    public CollectionRegionAccessStrategyAdapter(final AccessDelegate<? extends HazelcastCollectionRegion> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void evict(final Object key) throws CacheException {
        delegate.evict(key);
    }

    @Override
    public void evictAll() throws CacheException {
        delegate.evictAll();
    }

    @Override
    public Object get(final Object key, final long txTimestamp) throws CacheException {
        return delegate.get(key, txTimestamp);
    }

    @Override
    public CollectionRegion getRegion() {
        return delegate.getHazelcastRegion();
    }

    @Override
    public SoftLock lockItem(final Object key, final Object version) throws CacheException {
        return delegate.lockItem(key, version);
    }

    @Override
    public SoftLock lockRegion() throws CacheException {
        return delegate.lockRegion();
    }

    @Override
    public boolean putFromLoad(final Object key, final Object value, final long txTimestamp, final Object version)
            throws CacheException {
        return delegate.putFromLoad(key, value, txTimestamp, version);
    }

    @Override
    public boolean putFromLoad(final Object key, final Object value, final long txTimestamp, final Object version,
                               final boolean minimalPutOverride) throws CacheException {
        return delegate.putFromLoad(key, value, txTimestamp, version, minimalPutOverride);
    }

    @Override
    public void remove(final Object key) throws CacheException {
        delegate.remove(key);
    }

    @Override
    public void removeAll() throws CacheException {
        delegate.removeAll();
    }

    @Override
    public void unlockItem(final Object key, final SoftLock lock) throws CacheException {
        delegate.unlockItem(key, lock);
    }

    @Override
    public void unlockRegion(final SoftLock lock) throws CacheException {
        delegate.unlockRegion(lock);
    }
}
