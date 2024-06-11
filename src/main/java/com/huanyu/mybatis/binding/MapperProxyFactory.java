package com.huanyu.mybatis.binding;

import com.huanyu.mybatis.session.SqlSession;

import java.lang.reflect.Proxy;

/**
 * ClassName: MapperProxyFactory
 * Package: com.huanyu.mybatis.binding
 * Description:映射器代理工厂
 *
 * @Author: 寰宇
 * @Create: 2024/6/11 15:48
 * @Version: 1.0
 */
public class MapperProxyFactory<T> {
    private final Class<T> mapperInterface;

    public MapperProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    @SuppressWarnings("unchecked")
    public T newInstance(SqlSession sqlSession) {
        final MapperProxy<T> mapperProxy = new MapperProxy<>(sqlSession, mapperInterface);
        return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[]{mapperInterface}, mapperProxy);
    }

}
