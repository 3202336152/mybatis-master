package com.huanyu.mybatis.cache;

import com.huanyu.mybatis.cache.decorators.TransactionalCache;

import java.util.HashMap;
import java.util.Map;

/**
 * ClassName: TransactionalCacheManager
 * Package: com.huanyu.mybatis.cache
 * Description: 事务缓存，管理器
 *
 * @Author: 寰宇
 * @Create: 2024/6/29 15:34
 * @Version: 1.0
 */
public class TransactionalCacheManager {

    // 管理多个缓存的映射
    private Map<Cache, TransactionalCache> transactionalCaches = new HashMap<>();

    public void clear(Cache cache) {
        getTransactionalCache(cache).clear();
    }

    /**
     * 得到某个TransactionalCache的值
     */
    public Object getObject(Cache cache, CacheKey key) {
        return getTransactionalCache(cache).getObject(key);
    }

    public void putObject(Cache cache, CacheKey key, Object value) {
        getTransactionalCache(cache).putObject(key, value);
    }

    /**
     * 提交时全部提交
     */
    public void commit() {
        for (TransactionalCache txCache : transactionalCaches.values()) {
            txCache.commit();
        }
    }

    /**
     * 回滚时全部回滚
     */
    public void rollback() {
        for (TransactionalCache txCache : transactionalCaches.values()) {
            txCache.rollback();
        }
    }

    private TransactionalCache getTransactionalCache(Cache cache) {
        // 从事务缓存映射中获取指定缓存对应的事务缓存对象
        TransactionalCache txCache = transactionalCaches.get(cache);
        // 如果事务缓存对象为空，表示该缓存尚未被事务缓存管理，需要创建新的事务缓存对象
        if (txCache == null) {
            // 创建新的事务缓存对象，并将其放入事务缓存映射中
            txCache = new TransactionalCache(cache);
            // 放入缓存
            transactionalCaches.put(cache, txCache);
        }
        // 返回获取到的事务缓存对象
        return txCache;
    }

}
