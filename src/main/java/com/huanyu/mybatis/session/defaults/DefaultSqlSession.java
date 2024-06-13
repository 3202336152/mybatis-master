package com.huanyu.mybatis.session.defaults;

import com.huanyu.mybatis.mapping.BoundSql;
import com.huanyu.mybatis.mapping.Environment;
import com.huanyu.mybatis.mapping.MappedStatement;
import com.huanyu.mybatis.session.Configuration;
import com.huanyu.mybatis.session.SqlSession;

import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: DefaultSqlSession
 * Package: com.huanyu.mybatis.session.defaults
 * Description: 默认SqlSession实现类
 *
 * @Author: 寰宇
 * @Create: 2024/6/11 16:00
 * @Version: 1.0
 */
public class DefaultSqlSession implements SqlSession {

    // 配置信息
    private Configuration configuration;

    public DefaultSqlSession(Configuration configuration) {
        this.configuration = configuration;
    }
    @Override
    public <T> T selectOne(String statement) {
        return (T) ("你被代理了！" + statement);
    }

    // 根据传入的 SQL 语句和参数执行查询，并返回一个结果对象。
    @Override
    public <T> T selectOne(String statement, Object parameter) {
        try {
            // 获取 MappedStatement 对象，通过配置文件中的 statement 名称
            MappedStatement mappedStatement = configuration.getMappedStatement(statement);
            // 获取数据库环境信息
            Environment environment = configuration.getEnvironment();
            // 获取数据库连接
            Connection connection = environment.getDataSource().getConnection();
            // 获取 SQL 语句及其绑定的参数信息
            BoundSql boundSql = mappedStatement.getBoundSql();
            // 准备 SQL 语句
            PreparedStatement preparedStatement = connection.prepareStatement(boundSql.getSql());
            // 设置 SQL 语句的参数（假设第一个参数是 Long 类型）
            preparedStatement.setLong(1, Long.parseLong(((Object[]) parameter)[0].toString()));
            // 执行查询操作
            ResultSet resultSet = preparedStatement.executeQuery();
            // 将结果集转换为对象列表
            List<T> objList = resultSet2Obj(resultSet, Class.forName(boundSql.getResultType()));
            // 返回结果列表中的第一个对象
            return objList.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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

    @Override
    public <T> T getMapper(Class<T> type) {
        return configuration.getMapper(type, this);
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }
}
