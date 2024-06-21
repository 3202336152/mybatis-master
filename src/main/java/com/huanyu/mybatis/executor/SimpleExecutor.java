package com.huanyu.mybatis.executor;

import com.huanyu.mybatis.executor.statement.StatementHandler;
import com.huanyu.mybatis.mapping.BoundSql;
import com.huanyu.mybatis.mapping.MappedStatement;
import com.huanyu.mybatis.session.Configuration;
import com.huanyu.mybatis.session.ResultHandler;
import com.huanyu.mybatis.session.RowBounds;
import com.huanyu.mybatis.transaction.Transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * ClassName: SimpleExecutor
 * Package: com.huanyu.mybatis.executor
 * Description: 简单执行器
 *
 * @Author: 寰宇
 * @Create: 2024/6/15 16:14
 * @Version: 1.0
 */
public class SimpleExecutor extends BaseExecutor{

    public SimpleExecutor(Configuration configuration, Transaction transaction) {
        super(configuration, transaction);
    }

    @Override
    protected <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        try {
            Configuration configuration = ms.getConfiguration();
            // 新建一个 StatementHandler
            StatementHandler handler = configuration.newStatementHandler(this, ms, parameter, rowBounds, resultHandler, boundSql);
            // 获取数据库连接
            Connection connection = transaction.getConnection();
            // Statement是用于执行静态SQL语句并返回其生成的结果的对象
            Statement stmt = handler.prepare(connection);
            // 设置 SQL 语句的参数（假设第一个参数是 Long 类型）
            handler.parameterize(stmt);
            // 执行查询操作
            return handler.query(stmt, resultHandler);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

}
