package com.huanyu.mybatis.transaction;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * ClassName: Transaction
 * Package: com.huanyu.mybatis.transaction
 * Description: 事务接口
 *
 * @Author: 寰宇
 * @Create: 2024/6/13 16:09
 * @Version: 1.0
 */
public interface Transaction {

    /**
     * 获取该事务对应的数据库连接
     * @return 数据库连接
     * @throws SQLException
     */
    Connection getConnection() throws SQLException;

    /**
     * 提交事务
     * @throws SQLException
     */
    void commit() throws SQLException;

    /**
     * 回滚事务
     * @throws SQLException
     */
    void rollback() throws SQLException;

    /**
     * 关闭对应的数据连接
     * @throws SQLException
     */
    void close() throws SQLException;
}
