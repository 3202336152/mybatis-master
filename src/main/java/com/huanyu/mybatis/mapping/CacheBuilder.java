package com.huanyu.mybatis.mapping;

import com.huanyu.mybatis.cache.Cache;
import com.huanyu.mybatis.cache.decorators.FifoCache;
import com.huanyu.mybatis.cache.impl.PerpetualCache;
import com.huanyu.mybatis.reflection.MetaObject;
import com.huanyu.mybatis.reflection.SystemMetaObject;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * ClassName: CacheBuilder
 * Package: com.huanyu.mybatis.mapping
 * Description: 缓存构建器，建造者模式
 *
 * @Author: 寰宇
 * @Create: 2024/6/29 15:46
 * @Version: 1.0
 */
public class CacheBuilder {

    // Cache的编号
    private final String id;
    // Cache的实现类
    private Class<? extends Cache> implementation;
    // Cache的装饰器列表
    private final List<Class<? extends Cache>> decorators;
    // Cache的大小
    private Integer size;
    // Cache的清理间隔
    private Long clearInterval;
    // Cache是否可读写
    private boolean readWrite;
    // Cache的配置信息
    private Properties properties;
    // Cache是否阻塞
    private boolean blocking;

    public CacheBuilder(String id) {
        this.id = id;
        this.decorators = new ArrayList<>();
    }

    public CacheBuilder implementation(Class<? extends Cache> implementation) {
        this.implementation = implementation;
        return this;
    }

    public CacheBuilder addDecorator(Class<? extends Cache> decorator) {
        if (decorator != null) {
            this.decorators.add(decorator);
        }
        return this;
    }

    public CacheBuilder size(Integer size) {
        this.size = size;
        return this;
    }

    public CacheBuilder clearInterval(Long clearInterval) {
        this.clearInterval = clearInterval;
        return this;
    }

    public CacheBuilder readWrite(boolean readWrite) {
        this.readWrite = readWrite;
        return this;
    }

    public CacheBuilder blocking(boolean blocking) {
        this.blocking = blocking;
        return this;
    }

    public CacheBuilder properties(Properties properties) {
        this.properties = properties;
        return this;
    }

    /**
     * 组建缓存
     * @return 缓存对象
     */
    public Cache build() {
        // 设置缓存的默认实现、默认装饰器（仅设置，并未装配）
        setDefaultImplementations();
        // 创建默认的缓存
        Cache cache = newBaseCacheInstance(implementation, id);
        // 设置缓存的属性
        setCacheProperties(cache);
        if (PerpetualCache.class.equals(cache.getClass())) { // 缓存实现是PerpetualCache，即不是用户自定义的缓存实现
            // 为缓存逐级嵌套自定义的装饰器
            for (Class<? extends Cache> decorator : decorators) {
                // 生成装饰器实例，并装配。入参依次是装饰器类、被装饰的缓存
                cache = newCacheDecoratorInstance(decorator, cache);
                // 为装饰器设置属性
                setCacheProperties(cache);
            }
        }
        // 返回被包装好的缓存
        return cache;
    }

    /**
     * 设置缓存的默认实现和默认装饰器
     */
    private void setDefaultImplementations() {
        if (implementation == null) {
            implementation = PerpetualCache.class;
            if (decorators.isEmpty()) {
                decorators.add(FifoCache.class);
            }
        }
    }

    /**
     * 设置缓存的属性值
     */
    private void setCacheProperties(Cache cache) {
        if (properties != null) {
            MetaObject metaCache = SystemMetaObject.forObject(cache);
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                String name = (String) entry.getKey();
                String value = (String) entry.getValue();
                if (metaCache.hasSetter(name)) {
                    Class<?> type = metaCache.getSetterType(name);
                    if (String.class == type) {
                        metaCache.setValue(name, value);
                    } else if (int.class == type
                            || Integer.class == type) {
                        metaCache.setValue(name, Integer.valueOf(value));
                    } else if (long.class == type
                            || Long.class == type) {
                        metaCache.setValue(name, Long.valueOf(value));
                    } else if (short.class == type
                            || Short.class == type) {
                        metaCache.setValue(name, Short.valueOf(value));
                    } else if (byte.class == type
                            || Byte.class == type) {
                        metaCache.setValue(name, Byte.valueOf(value));
                    } else if (float.class == type
                            || Float.class == type) {
                        metaCache.setValue(name, Float.valueOf(value));
                    } else if (boolean.class == type
                            || Boolean.class == type) {
                        metaCache.setValue(name, Boolean.valueOf(value));
                    } else if (double.class == type
                            || Double.class == type) {
                        metaCache.setValue(name, Double.valueOf(value));
                    } else {
                        throw new RuntimeException("Unsupported property type for cache: '" + name + "' of type " + type);
                    }
                }
            }
        }
    }

    private Cache newBaseCacheInstance(Class<? extends Cache> cacheClass, String id) {
        Constructor<? extends Cache> cacheConstructor = getBaseCacheConstructor(cacheClass);
        try {
            return cacheConstructor.newInstance(id);
        } catch (Exception e) {
            throw new RuntimeException("Could not instantiate cache implementation (" + cacheClass + "). Cause: " + e, e);
        }
    }

    private Constructor<? extends Cache> getBaseCacheConstructor(Class<? extends Cache> cacheClass) {
        try {
            return cacheClass.getConstructor(String.class);
        } catch (Exception e) {
            throw new RuntimeException("Invalid base cache implementation (" + cacheClass + ").  " +
                    "Base cache implementations must have a constructor that takes a String id as a parameter.  Cause: " + e, e);
        }
    }

    private Cache newCacheDecoratorInstance(Class<? extends Cache> cacheClass, Cache base) {
        Constructor<? extends Cache> cacheConstructor = getCacheDecoratorConstructor(cacheClass);
        try {
            return cacheConstructor.newInstance(base);
        } catch (Exception e) {
            throw new RuntimeException("Could not instantiate cache decorator (" + cacheClass + "). Cause: " + e, e);
        }
    }

    private Constructor<? extends Cache> getCacheDecoratorConstructor(Class<? extends Cache> cacheClass) {
        try {
            return cacheClass.getConstructor(Cache.class);
        } catch (Exception e) {
            throw new RuntimeException("Invalid cache decorator (" + cacheClass + ").  " +
                    "Cache decorators must have a constructor that takes a Cache instance as a parameter.  Cause: " + e, e);
        }
    }
}
