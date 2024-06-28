package com.huanyu.mybatis.plugin;

import com.huanyu.mybatis.executor.statement.StatementHandler;
import com.huanyu.mybatis.mapping.BoundSql;
import com.huanyu.mybatis.mapping.ParameterMapping;
import com.huanyu.mybatis.session.ResultHandler;

import java.lang.reflect.Field;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Properties;

// 拦截器注解:拦截的类、类中的方法、参数
@Intercepts({@Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class})})
public class QueryPlugin implements Interceptor {

    private long threshold; // 阈值，超过该时间的SQL会被记录

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        long startTime = System.currentTimeMillis(); // 记录开始时间
        Object result = invocation.proceed(); // 执行原方法
        long endTime = System.currentTimeMillis(); // 记录结束时间
        long time = endTime - startTime; // 计算耗时

        // 判断是否超过阈值
        if (time > threshold) {
            // 获取StatementHandler
            StatementHandler handler = (StatementHandler) invocation.getTarget();
            // 获取BoundSql对象
            BoundSql boundSql = handler.getBoundSql();
            // 获取SQL字符串
            String sql = boundSql.getSql();
            // 获取参数信息
            Object parameterObject = boundSql.getParameterObject();
            // 获取参数映射
            List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
            // 替换SQL中的参数占位符
            String completeSql = getCompleteSql(sql, parameterObject, parameterMappings);
            System.out.println("SQL执行耗时：" + time + " ms");
            System.out.println("SQL语句：" + completeSql);
        }

        return result;
    }

    @Override
    public void setProperties(Properties properties) {
        String thresholdStr = properties.getProperty("threshold");
        this.threshold = Long.parseLong(thresholdStr); // 设置阈值
    }

    /**
     * 获取完整的SQL语句，包含参数值
     */
    private String getCompleteSql(String sql, Object parameterObject, List<ParameterMapping> parameterMappings) {
        // 如果没有参数映射或参数对象为null，直接返回原始SQL
        if (parameterMappings == null || parameterMappings.isEmpty() || parameterObject == null) {
            return sql;
        }
        // 创建StringBuilder用于构建完整的SQL
        StringBuilder completeSql = new StringBuilder(sql);

        try {
            // 遍历所有参数映射
            for (ParameterMapping parameterMapping : parameterMappings) {
                // 获取参数属性名
                String propertyName = parameterMapping.getProperty();
                // 获取参数值
                Object value = getPropertyValue(parameterObject, propertyName);

                // 将第一个问号占位符替换为实际参数值
                completeSql = new StringBuilder(completeSql.toString().replaceFirst("\\?", value.toString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 返回包含参数值的完整SQL
        return completeSql.toString();
    }

    /**
     * 通过反射获取参数值
     */
    private Object getPropertyValue(Object parameterObject, String propertyName) throws Exception {
        // 如果参数对象是Map，直接从Map中获取值
        if (parameterObject instanceof Map) {
            return ((Map<?, ?>) parameterObject).get(propertyName);
        } else {
            // 否则通过反射获取对象的属性值
            Field field = parameterObject.getClass().getDeclaredField(propertyName); // 获取属性对象
            field.setAccessible(true); // 设置属性可访问
            return field.get(parameterObject); // 返回属性值
        }
    }

}

