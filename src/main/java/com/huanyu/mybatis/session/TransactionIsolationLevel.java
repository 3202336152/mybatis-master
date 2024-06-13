package com.huanyu.mybatis.session;

import java.sql.Connection;

/**
 * ClassName: TransactionIsolationLevel
 * Package: com.huanyu.mybatis.session
 * Description: 事务的隔离级别
 *
 * @Author: 寰宇
 * @Create: 2024/6/13 16:09
 * @Version: 1.0
 */
public enum TransactionIsolationLevel {
    //包括JDBC支持的5个级别
    NONE(Connection.TRANSACTION_NONE),
    READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
    READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
    REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
    SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);

    private final int level;

    TransactionIsolationLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
