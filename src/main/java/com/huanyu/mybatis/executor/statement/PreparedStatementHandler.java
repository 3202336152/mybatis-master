package com.huanyu.mybatis.executor.statement;

import com.huanyu.mybatis.executor.Executor;
import com.huanyu.mybatis.mapping.BoundSql;
import com.huanyu.mybatis.mapping.MappedStatement;
import com.huanyu.mybatis.session.ResultHandler;
import com.huanyu.mybatis.session.RowBounds;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * ClassName: PreparedStatementHandler
 * Package: com.huanyu.mybatis.executor.statement
 * Description: 预处理语句处理器（PREPARED）
 *
 * @Author: 寰宇
 * @Create: 2024/6/15 16:18
 * @Version: 1.0
 */
public class PreparedStatementHandler extends BaseStatementHandler {

    public PreparedStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        super(executor, mappedStatement, parameterObject, resultHandler, rowBounds, boundSql);
    }

    @Override
    protected Statement instantiateStatement(Connection connection) throws SQLException {
        String sql = boundSql.getSql();
        return connection.prepareStatement(sql);
    }

    @Override
    public void parameterize(Statement statement) throws SQLException {
        parameterHandler.setParameters((PreparedStatement) statement);
    }

    @Override
    public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
        PreparedStatement ps = (PreparedStatement) statement;
        // 执行真正的查询，查询完成后，结果就在ps中了
        ps.execute();
        // 由resultSetHandler继续处理结果
        return resultSetHandler.<E>handleResultSets(ps);
    }
}
