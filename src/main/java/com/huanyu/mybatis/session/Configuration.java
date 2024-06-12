package com.huanyu.mybatis.session;

import com.huanyu.mybatis.binding.MapperRegistry;
import com.huanyu.mybatis.mapping.MappedStatement;

import java.util.HashMap;
import java.util.Map;

/**
 * ClassName: Configuration
 * Package: com.huanyu.mybatis.session
 * Description: 配置项
 * 主要内容分为以下几个部分：
 * 1、大量的配置项，和与`<configuration>`标签中的配置对应
 * 2、创建类型别名注册机，并向内注册了大量的类型别名
 * 3、创建了大量Map，包括存储映射语句的Map，存储缓存的Map等，这些Map使用的是一种不允许覆盖的严格Map
 * 4、给出了大量的处理器的创建方法，包括参数处理器、语句处理器、结果处理器、执行器。
 * 这里并没有真正创建，只是给出了方法。
 * @Author: 寰宇
 * @Create: 2024/6/12 14:37
 * @Version: 1.0
 */
public class Configuration {
    /**
     * 映射注册机
     */
    protected MapperRegistry mapperRegistry = new MapperRegistry(this);

    /**
     * 映射的语句，存在Map里
     */
    protected final Map<String, MappedStatement> mappedStatements = new HashMap<>();

    public void addMappers(String packageName) {
        mapperRegistry.addMappers(packageName);
    }

    public <T> void addMapper(Class<T> type) {
        mapperRegistry.addMapper(type);
    }

    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        return mapperRegistry.getMapper(type, sqlSession);
    }

    public boolean hasMapper(Class<?> type) {
        return mapperRegistry.hasMapper(type);
    }

    public void addMappedStatement(MappedStatement ms) {
        mappedStatements.put(ms.getId(), ms);
    }

    public MappedStatement getMappedStatement(String id) {
        return mappedStatements.get(id);
    }
}
