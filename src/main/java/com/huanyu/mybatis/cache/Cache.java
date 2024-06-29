package com.huanyu.mybatis.cache;

/**
 * ClassName: Cache
 * Package: com.huanyu.mybatis.cache
 * Description: SPI(Service Provider Interface) for cache providers. 缓存接口
 *
 * @Author: 寰宇
 * @Create: 2024/6/28 16:54
 * @Version: 1.0
 */

public interface Cache {

    /**
     * 获取ID，每个缓存都有唯一ID标识
     */
    String getId();

    /**
     * 存入值
     */
    void putObject(Object key, Object value);

    /**
     * 获取值
     */
    Object getObject(Object key);

    /**
     * 删除值
     */
    Object removeObject(Object key);

    /**
     * 清空
     */
    void clear();

    /**
     * 获取缓存大小
     */
    int getSize();
}
