package com.huanyu.mybatis.executor.resultset;

import com.huanyu.mybatis.executor.Executor;
import com.huanyu.mybatis.mapping.BoundSql;
import com.huanyu.mybatis.mapping.MappedStatement;

import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: DefaultResultSetHandler
 * Package: com.huanyu.mybatis.executor.resultset
 * Description: 默认Map结果处理器
 *
 * @Author: 寰宇
 * @Create: 2024/6/15 16:20
 * @Version: 1.0
 */
public class DefaultResultSetHandler implements ResultSetHandler{

    private final BoundSql boundSql;

    private final MappedStatement mappedStatement;


    public DefaultResultSetHandler(Executor executor, MappedStatement mappedStatement, BoundSql boundSql) {
        this.boundSql = boundSql;
        this.mappedStatement = mappedStatement;
    }

    /**
     * 处理Statement得到的多结果集（也可能是单结果集，这是多结果集的一种简化形式），最终得到结果列表
     * @param stmt Statement语句
     * @return 结果列表
     * @throws SQLException
     */
    @Override
    public <E> List<E> handleResultSets(Statement stmt) throws SQLException {
        ResultSet resultSet = stmt.getResultSet();
        // 将结果集转换为对象列表
        return resultSet2Obj(resultSet, mappedStatement.getResultType());
    }

    // 将 ResultSet 对象转换为特定类型的对象列表。
    private <T> List<T> resultSet2Obj(ResultSet resultSet, Class<?> clazz) {
        List<T> list = new ArrayList<>();
        try {
            // 获取结果集的元数据
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            // 遍历结果集的每一行
            while (resultSet.next()) {
                // 创建一个新实例
                T obj = (T) clazz.newInstance();
                // 遍历每一列
                for (int i = 1; i <= columnCount; i++) {
                    // 获取列值
                    Object value = resultSet.getObject(i);
                    // 获取列名
                    String columnName = metaData.getColumnName(i);
                    // 构建 setter 方法名
                    String setMethod = "set" + columnName.substring(0, 1).toUpperCase() + columnName.substring(1);
                    Method method;
                    // 如果列值是时间戳类型，使用 Date 类型的 setter 方法
                    if (value instanceof Timestamp) {
                        method = clazz.getMethod(setMethod, Date.class);
                    } else {
                        // 否则，使用列值类型的 setter 方法
                        method = clazz.getMethod(setMethod, value.getClass());
                    }
                    // 调用 setter 方法将列值赋值给对象
                    method.invoke(obj, value);
                }
                // 将对象添加到列表中
                list.add(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
