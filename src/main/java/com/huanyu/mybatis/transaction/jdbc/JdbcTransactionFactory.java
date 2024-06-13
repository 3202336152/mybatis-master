package com.huanyu.mybatis.transaction.jdbc;

import com.huanyu.mybatis.session.TransactionIsolationLevel;
import com.huanyu.mybatis.transaction.Transaction;
import com.huanyu.mybatis.transaction.TransactionFactory;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * ClassName: JdbcTransactionFactory
 * Package: com.huanyu.mybatis.transaction.jdbc
 * Description: JdbcTransaction 工厂
 *
 * @Author: 寰宇
 * @Create: 2024/6/13 16:11
 * @Version: 1.0
 */
public class JdbcTransactionFactory implements TransactionFactory {
    @Override
    public Transaction newTransaction(Connection conn) {
        return new JdbcTransaction(conn);
    }

    @Override
    public Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit) {
        return new JdbcTransaction(dataSource, level, autoCommit);
    }
}
