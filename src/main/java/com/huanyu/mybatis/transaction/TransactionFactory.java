package com.huanyu.mybatis.transaction;

import com.huanyu.mybatis.session.TransactionIsolationLevel;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * ClassName: TransactionFactory
 * Package: com.huanyu.mybatis.transaction
 * Description: 事务工厂
 *
 * @Author: 寰宇
 * @Create: 2024/6/13 16:09
 * @Version: 1.0
 */
public interface TransactionFactory {
    /**
     * 根据 Connection 创建 Transaction
     * @param conn Existing database connection
     * @return Transaction
     */
    Transaction newTransaction(Connection conn);

    /**
     * 根据数据源和事务隔离级别创建 Transaction
     * @param dataSource DataSource to take the connection from
     * @param level Desired isolation level
     * @param autoCommit Desired autocommit
     * @return Transaction
     */
    Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit);

}
