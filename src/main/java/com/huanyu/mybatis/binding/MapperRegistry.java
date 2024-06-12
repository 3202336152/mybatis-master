package com.huanyu.mybatis.binding;

import cn.hutool.core.lang.ClassScanner;
import com.huanyu.mybatis.session.Configuration;
import com.huanyu.mybatis.session.SqlSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * ClassName: MapperRegistry
 * Package: com.huanyu.mybatis.binding
 * Description: 映射器注册机
 * 被Configuration持有，存着。
 * @Author: 寰宇
 * @Create: 2024/6/11 15:50
 * @Version: 1.0
 */
public class MapperRegistry {

    private Configuration config;

    public MapperRegistry(Configuration config) {
        this.config = config;
    }

    /**
     * 将已添加的映射器代理加入到 HashMap
     * 已知的所有映射
     * key:mapperInterface,即dao的数据库接口，不是方法
     * value:MapperProxyFactory,即映射器代理工厂
     */
    private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<>();

    /**
     * 找到指定映射接口的映射文件，并根据映射文件信息为该映射接口生成一个代理实现
     * @param type 映射接口
     * @param sqlSession sqlSession
     * @param <T> 映射接口类型
     * @return 代理实现对象
     */
    @SuppressWarnings("unchecked")
    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        // 找出指定映射接口的代理工厂
        final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
        if (mapperProxyFactory == null) {
            throw new RuntimeException("Type " + type + " is not known to the MapperRegistry.");
        }
        try {
            // 通过mapperProxyFactory给出对应代理器的实例
            return mapperProxyFactory.newInstance(sqlSession);
        } catch (Exception e) {
            throw new RuntimeException("Error getting mapper instance. Cause: " + e, e);
        }
    }

    public <T> void addMapper(Class<T> type) {
        /* Mapper 必须是接口才会注册 */
        if (type.isInterface()) {
            if (hasMapper(type)) {
                // 如果重复添加了，报错
                throw new RuntimeException("Type " + type + " is already known to the MapperRegistry.");
            }
            // 注册映射器代理工厂
            knownMappers.put(type, new MapperProxyFactory<>(type));
        }
    }

    public <T> boolean hasMapper(Class<T> type) {
        return knownMappers.containsKey(type);
    }

    // 添加映射器代理类
    public void addMappers(String packageName) {
        // 扫描包
        Set<Class<?>> mapperSet = ClassScanner.scanPackage(packageName);
        for (Class<?> mapperClass : mapperSet) {
            addMapper(mapperClass);
        }
    }

}
