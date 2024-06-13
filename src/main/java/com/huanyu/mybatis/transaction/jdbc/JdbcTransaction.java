package com.huanyu.mybatis.transaction.jdbc;

import com.huanyu.mybatis.session.TransactionIsolationLevel;
import com.huanyu.mybatis.transaction.Transaction;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * ClassName: JdbcTransaction
 * Package: com.huanyu.mybatis.transaction.jdbc
 * Description: JDBC 事务，直接利用 JDBC 的 commit、rollback。依赖于数据源获得的连接来管理事务范围。
 * @Author: 寰宇
 * @Create: 2024/6/13 16:10
 * @Version: 1.0
*/
public class JdbcTransaction implements Transaction {

    // 数据库连接
    protected Connection connection;

    // 数据源
    protected DataSource dataSource;

    // 事务隔离级别
    protected TransactionIsolationLevel level = TransactionIsolationLevel.NONE;

    // 是否自动提交事务
    protected boolean autoCommit;

    public JdbcTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit) {
        this.dataSource = dataSource;
        this.level = level;
        this.autoCommit = autoCommit;
    }

    public JdbcTransaction(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Connection getConnection() throws SQLException {
        connection = dataSource.getConnection();
        connection.setTransactionIsolation(level.getLevel());
        connection.setAutoCommit(autoCommit);
        return connection;
    }

    @Override
    public void commit() throws SQLException {
        if (connection != null && !connection.getAutoCommit()) {
            connection.commit();
        }
    }

    @Override
    public void rollback() throws SQLException {
        if (connection != null && !connection.getAutoCommit()) {
            connection.rollback();
        }
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.getAutoCommit()) {
            connection.close();
        }
    }
}
