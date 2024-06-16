package com.huanyu.mybatis.executor.statement;

import com.huanyu.mybatis.executor.Executor;
import com.huanyu.mybatis.executor.resultset.ResultSetHandler;
import com.huanyu.mybatis.mapping.BoundSql;
import com.huanyu.mybatis.mapping.MappedStatement;
import com.huanyu.mybatis.session.Configuration;
import com.huanyu.mybatis.session.ResultHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * ClassName: BaseStatementHandler
 * Package: com.huanyu.mybatis.executor.statement
 * Description: 语句处理器抽象基类
 * 主要定义了从Connection中获取Statement的方法，而对于具体的Statement操作则未定义
 * @Author: 寰宇
 * @Create: 2024/6/15 16:16
 * @Version: 1.0
 */
public abstract class BaseStatementHandler implements StatementHandler{

    protected final Configuration configuration;
    protected final Executor executor;
    protected final MappedStatement mappedStatement;

    protected final Object parameterObject;
    protected final ResultSetHandler resultSetHandler;

    protected BoundSql boundSql;

    public BaseStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, ResultHandler resultHandler, BoundSql boundSql) {
        this.configuration = mappedStatement.getConfiguration();
        this.executor = executor;
        this.mappedStatement = mappedStatement;
        this.boundSql = boundSql;

        this.parameterObject = parameterObject;
        this.resultSetHandler = configuration.newResultSetHandler(executor, mappedStatement, boundSql);
    }

    // 从连接中获取一个Statement，并设置事务超时时间
    @Override
    public Statement prepare(Connection connection) throws SQLException {
        Statement statement = null;
        try {
            // 实例化 Statement
            statement = instantiateStatement(connection);
            // 参数设置，可以被抽取，提供配置
            statement.setQueryTimeout(350);
            statement.setFetchSize(10000);
            return statement;
        } catch (Exception e) {
            throw new RuntimeException("Error preparing statement.  Cause: " + e, e);
        }
    }

    // 从Connection中实例化Statement
    protected abstract Statement instantiateStatement(Connection connection) throws SQLException;

}
