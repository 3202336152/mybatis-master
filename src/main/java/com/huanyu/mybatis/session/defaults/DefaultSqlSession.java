package com.huanyu.mybatis.session.defaults;

import com.huanyu.mybatis.executor.Executor;
import com.huanyu.mybatis.mapping.BoundSql;
import com.huanyu.mybatis.mapping.Environment;
import com.huanyu.mybatis.mapping.MappedStatement;
import com.huanyu.mybatis.session.Configuration;
import com.huanyu.mybatis.session.SqlSession;

import java.sql.*;
import java.util.List;

/**
 * ClassName: DefaultSqlSession
 * Package: com.huanyu.mybatis.session.defaults
 * Description: 默认SqlSession实现类
 *
 * @Author: 寰宇
 * @Create: 2024/6/11 16:00
 * @Version: 1.0
 */
public class DefaultSqlSession implements SqlSession {

    // 配置信息
    private Configuration configuration;

    // 执行器
    private Executor executor;

    public DefaultSqlSession(Configuration configuration, Executor executor) {
        this.configuration = configuration;
        this.executor = executor;
    }

    @Override
    public <T> T selectOne(String statement) {
        return this.selectOne(statement, null);
    }

    // 根据传入的 SQL 语句和参数执行查询，并返回一个结果对象。
    @Override
    public <T> T selectOne(String statement, Object parameter) {

        // 获取 MappedStatement 对象，通过配置文件中的 statement 名称
        MappedStatement mappedStatement = configuration.getMappedStatement(statement);
        // 执行器执行查询操作
        List<T> list = executor.query(mappedStatement, parameter, Executor.NO_RESULT_HANDLER, mappedStatement.getSqlSource().getBoundSql(parameter));
        // 返回结果列表中的第一个对象
        return list.get(0);

    }


    @Override
    public <T> T getMapper(Class<T> type) {
        return configuration.getMapper(type, this);
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }
}
