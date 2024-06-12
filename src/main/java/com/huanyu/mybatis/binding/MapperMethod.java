package com.huanyu.mybatis.binding;

import com.huanyu.mybatis.mapping.MappedStatement;
import com.huanyu.mybatis.mapping.SqlCommandType;
import com.huanyu.mybatis.session.Configuration;
import com.huanyu.mybatis.session.SqlSession;

import java.lang.reflect.Method;

/**
 * ClassName: MapperMethod
 * Package: com.huanyu.mybatis.binding
 * Description: 映射器方法
 *
 * @Author: 寰宇
 * @Create: 2024/6/12 14:31
 * @Version: 1.0
 */
public class MapperMethod {

    // 记录了sql的名称和类型
    private final SqlCommand command;

    /**
     * MapperMethod的构造方法
     * @param mapperInterface 映射接口
     * @param method 映射接口中的具体方法
     * @param configuration 配置信息Configuration
     */
    public MapperMethod(Class<?> mapperInterface, Method method, Configuration configuration) {
        this.command = new SqlCommand(configuration, mapperInterface, method);
    }

    /**
     * 执行映射接口中的方法
     * @param sqlSession sqlSession接口的实例，通过它可以进行数据库的操作
     * @param args 执行接口方法时传入的参数
     * @return 数据库操作结果
     */
    public Object execute(SqlSession sqlSession, Object[] args) {
        Object result = null;
        // 根据SQL语句类型，执行不同操作
        switch (command.getType()) {
            case INSERT:
                break;
            case DELETE:
                break;
            case UPDATE:
                break;
            case SELECT:
                result = sqlSession.selectOne(command.getName(), args);
                break;
            default:
                throw new RuntimeException("Unknown execution method for: " + command.getName());
        }
        return result;
    }

    /**
     * SQL 指令
     */
    public static class SqlCommand {

        // SQL语句的名称
        private final String name;

        // SQL语句的种类，一共分为以下六种：增、删、改、查、清缓存、未知
        private final SqlCommandType type;

        public SqlCommand(Configuration configuration, Class<?> mapperInterface, Method method) {
            String statementName = mapperInterface.getName() + "." + method.getName();
            MappedStatement ms = configuration.getMappedStatement(statementName);
            name = ms.getId();
            type = ms.getSqlCommandType();
        }

        public String getName() {
            return name;
        }

        public SqlCommandType getType() {
            return type;
        }
    }
}
