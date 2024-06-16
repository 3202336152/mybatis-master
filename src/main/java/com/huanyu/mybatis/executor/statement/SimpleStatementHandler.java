package com.huanyu.mybatis.executor.statement;

import com.huanyu.mybatis.executor.Executor;
import com.huanyu.mybatis.mapping.BoundSql;
import com.huanyu.mybatis.mapping.MappedStatement;
import com.huanyu.mybatis.session.ResultHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * ClassName: SimpleStatementHandler
 * Package: com.huanyu.mybatis.executor.statement
 * Description: 简单语句处理器（STATEMENT）
 *
 * @Author: 寰宇
 * @Create: 2024/6/15 16:19
 * @Version: 1.0
 */
public class SimpleStatementHandler extends BaseStatementHandler{

    public SimpleStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, ResultHandler resultHandler, BoundSql boundSql) {
        // 调用BaseStatementHandler中的构造方法完成
        super(executor, mappedStatement, parameterObject, resultHandler, boundSql);
    }

    @Override
    protected Statement instantiateStatement(Connection connection) throws SQLException {
        return connection.createStatement();
    }

    @Override
    public void parameterize(Statement statement) throws SQLException {
        // N/A
    }

    @Override
    public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
        String sql = boundSql.getSql();
        statement.execute(sql);
        return resultSetHandler.handleResultSets(statement);
    }
}
