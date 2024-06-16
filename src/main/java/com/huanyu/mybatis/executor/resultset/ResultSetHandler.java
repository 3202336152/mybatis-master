package com.huanyu.mybatis.executor.resultset;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * ClassName: ResultSetHandler
 * Package: com.huanyu.mybatis.executor.resultset
 * Description: 结果集处理器
 *
 * @Author: 寰宇
 * @Create: 2024/6/15 16:17
 * @Version: 1.0
 */
public interface ResultSetHandler {

    // 将Statement的执行结果处理为List
    <E> List<E> handleResultSets(Statement stmt) throws SQLException;


}
