package com.huanyu.mybatis.cache.impl;

import com.huanyu.mybatis.cache.Cache;

import java.util.HashMap;
import java.util.Map;

/**
 * ClassName: PerpetualCache
 * Package: com.huanyu.mybatis.cache.impl
 * Description: 一级缓存，在 Session 生命周期内一直保持，每创建新的 OpenSession 都会创建一个缓存器 PerpetualCache
 *
 * @Author: 寰宇
 * @Create: 2024/6/28 16:56
 * @Version: 1.0
 */
public class PerpetualCache implements Cache {

    // Cache的id，一般为所在的namespace
    private String id;

    // 使用HashMap存放一级缓存数据，session 生命周期较短，正常情况下数据不会一直在缓存存放
    // 用来存储要缓存的信息
    private Map<Object, Object> cache = new HashMap<>();

    public PerpetualCache(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void putObject(Object key, Object value) {
        cache.put(key, value);
    }

    @Override
    public Object getObject(Object key) {
        return cache.get(key);
    }

    @Override
    public Object removeObject(Object key) {
        return cache.remove(key);
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public int getSize() {
        return cache.size();
    }
}
