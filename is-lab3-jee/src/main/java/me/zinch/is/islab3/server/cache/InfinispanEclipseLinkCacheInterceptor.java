package me.zinch.is.islab3.server.cache;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.internal.identitymaps.CacheKey;
import org.eclipse.persistence.internal.identitymaps.IdentityMap;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.sessions.interceptors.CacheInterceptor;
import org.eclipse.persistence.sessions.interceptors.CacheKeyInterceptor;

import java.util.Map;

public class InfinispanEclipseLinkCacheInterceptor extends CacheInterceptor {
    public InfinispanEclipseLinkCacheInterceptor(IdentityMap identityMap, AbstractSession session) {
        super(identityMap, session);
    }

    @Override
    protected CacheKeyInterceptor createCacheKeyInterceptor(CacheKey cacheKey) {
        return new CacheKeyInterceptor(cacheKey);
    }

    @Override
    public Map<Object, Object> getAllFromIdentityMapWithEntityPK(
            Object[] primaryKeys,
            ClassDescriptor descriptor,
            AbstractSession session
    ) {
        return targetIdentityMap.getAllFromIdentityMapWithEntityPK(primaryKeys, descriptor, session);
    }

    @Override
    public Map<Object, CacheKey> getAllCacheKeysFromIdentityMapWithEntityPK(
            Object[] primaryKeys,
            ClassDescriptor descriptor,
            AbstractSession session
    ) {
        return targetIdentityMap.getAllCacheKeysFromIdentityMapWithEntityPK(primaryKeys, descriptor, session);
    }

    @Override
    public Object get(Object primaryKey) {
        Object fromIdentityMap = targetIdentityMap.get(primaryKey);
        if (fromIdentityMap != null) {
            InfinispanL2CacheBridge.incHit();
            return fromIdentityMap;
        }

        Object fromInfinispan = InfinispanL2CacheBridge.get(getRegionName(), primaryKey);
        if (fromInfinispan != null) {
            InfinispanL2CacheBridge.incHit();
            targetIdentityMap.put(primaryKey, fromInfinispan, null, System.currentTimeMillis());
            return fromInfinispan;
        }

        InfinispanL2CacheBridge.incMiss();
        return null;
    }

    @Override
    public CacheKey getCacheKey(Object primaryKey, boolean forMerge) {
        CacheKey fromIdentityMap = targetIdentityMap.getCacheKey(primaryKey, forMerge);
        if (fromIdentityMap != null) {
            InfinispanL2CacheBridge.incHit();
            return fromIdentityMap;
        }

        Object fromInfinispan = InfinispanL2CacheBridge.get(getRegionName(), primaryKey);
        if (fromInfinispan != null) {
            InfinispanL2CacheBridge.incHit();
            targetIdentityMap.put(primaryKey, fromInfinispan, null, System.currentTimeMillis());
            return targetIdentityMap.getCacheKey(primaryKey, forMerge);
        }

        InfinispanL2CacheBridge.incMiss();
        return null;
    }

    @Override
    public CacheKey put(Object primaryKey, Object object, Object writeLockValue, long readTime) {
        InfinispanL2CacheBridge.put(getRegionName(), primaryKey, object);
        InfinispanL2CacheBridge.incPut();
        return targetIdentityMap.put(primaryKey, object, writeLockValue, readTime);
    }

    @Override
    public Object remove(Object primaryKey, Object object) {
        InfinispanL2CacheBridge.remove(getRegionName(), primaryKey);
        return targetIdentityMap.remove(primaryKey, object);
    }

    @Override
    public Object remove(CacheKey key) {
        if (key != null) {
            InfinispanL2CacheBridge.remove(getRegionName(), key.getKey());
        }
        return targetIdentityMap.remove(key);
    }

    @Override
    public Object clone() {
        return new InfinispanEclipseLinkCacheInterceptor((IdentityMap) targetIdentityMap.clone(), interceptedSession);
    }

    @Override
    public void release() {
        targetIdentityMap.release();
    }

    private String getRegionName() {
        Class<?> descriptorClass = getDescriptorClass();
        return descriptorClass == null ? "unknown" : descriptorClass.getName();
    }
}
