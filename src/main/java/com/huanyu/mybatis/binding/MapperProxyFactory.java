package com.huanyu.mybatis.binding;

import com.huanyu.mybatis.session.SqlSession;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    // 对应SQL的java接口类
    private final Class<T> mapperInterface;

    private Map<Method, MapperMethod> methodCache = new ConcurrentHashMap<Method, MapperMethod>();

    public Map<Method, MapperMethod> getMethodCache() {
        return methodCache;
    }

    public MapperProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    @SuppressWarnings("unchecked")
    public T newInstance(SqlSession sqlSession) {
        final MapperProxy<T> mapperProxy = new MapperProxy<>(sqlSession, mapperInterface, methodCache);
        // 三个参数分别是：
        // 创建代理对象的类加载器、要代理的接口、代理类的处理器（即具体的实现）。
        return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[]{mapperInterface}, mapperProxy);
    }

}
