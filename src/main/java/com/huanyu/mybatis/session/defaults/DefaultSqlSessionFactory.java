package com.huanyu.mybatis.session.defaults;

import com.huanyu.mybatis.binding.MapperRegistry;
import com.huanyu.mybatis.session.SqlSession;
import com.huanyu.mybatis.session.SqlSessionFactory;

/**
 * ClassName: DefaultSqlSessionFactory
 * Package: com.huanyu.mybatis.session.defaults
 * Description: 默认的SqlSessionFactory实现类
 *
 * @Author: 寰宇
 * @Create: 2024/6/11 16:00
 * @Version: 1.0
 */
public class DefaultSqlSessionFactory implements SqlSessionFactory {

    private final MapperRegistry mapperRegistry;

    public DefaultSqlSessionFactory(MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
    }

    // 默认的简单工厂实现，
    // 处理开启 SqlSession 时，对 DefaultSqlSession 的创建以及传递 mapperRegistry，
    // 这样就可以在使用 SqlSession 时获取每个代理类的映射器对象。
    @Override
    public SqlSession openSession() {
        return new DefaultSqlSession(mapperRegistry);
    }
}
