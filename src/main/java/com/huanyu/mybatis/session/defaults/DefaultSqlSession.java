package com.huanyu.mybatis.session.defaults;

import com.alibaba.fastjson.JSON;
import com.huanyu.mybatis.executor.Executor;
import com.huanyu.mybatis.mapping.MappedStatement;
import com.huanyu.mybatis.session.Configuration;
import com.huanyu.mybatis.session.RowBounds;
import com.huanyu.mybatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
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

    private Logger logger = LoggerFactory.getLogger(DefaultSqlSession.class);

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
        logger.info("执行查询 statement：{} parameter：{}", statement, JSON.toJSONString(parameter));
        // 获取 MappedStatement 对象，通过配置文件中的 statement 名称
        MappedStatement mappedStatement = configuration.getMappedStatement(statement);
        // 执行器执行查询操作
        List<T> list = executor.query(mappedStatement, parameter, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER, mappedStatement.getSqlSource().getBoundSql(parameter));
        // 返回结果列表中的第一个对象
        return list.get(0);

    }

    @Override
    public <E> List<E> selectList(String statement, Object parameter) {
        logger.info("执行查询 statement：{} parameter：{}", statement, JSON.toJSONString(parameter));
        MappedStatement ms = configuration.getMappedStatement(statement);
        return executor.query(ms, parameter, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER, ms.getSqlSource().getBoundSql(parameter));
    }

    @Override
    public int insert(String statement, Object parameter) {
        // 在 Mybatis 中 insert 调用的是 update
        return update(statement, parameter);
    }

    @Override
    public int update(String statement, Object parameter) {
        MappedStatement ms = configuration.getMappedStatement(statement);
        try {
            return executor.update(ms, parameter);
        } catch (SQLException e) {
            throw new RuntimeException("Error updating database.  Cause: " + e);
        }
    }

    @Override
    public Object delete(String statement, Object parameter) {
        return update(statement, parameter);
    }

    @Override
    public void commit() {
        try {
            executor.commit(true);
        } catch (SQLException e) {
            throw new RuntimeException("Error committing transaction.  Cause: " + e);
        }
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
