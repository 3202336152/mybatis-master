package com.huanyu.mybatis.executor.statement;

import com.huanyu.mybatis.session.ResultHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * ClassName: StatementHandler
 * Package: com.huanyu.mybatis.executor.statement
 * Description: 语句处理器
 *
 * @Author: 寰宇
 * @Create: 2024/6/15 16:15
 * @Version: 1.0
 */
public interface StatementHandler {

    // 从Connection中创建一个Statement
    Statement prepare(Connection connection) throws SQLException;

    // 为Statement绑定实参
    void parameterize(Statement statement) throws SQLException;

    // 执行更新
    int update(Statement statement) throws SQLException;

    // 执行查询操作，返回list
    <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException;

}
