package com.huanyu.mybatis.mapping;

import com.huanyu.mybatis.type.JdbcType;
import com.huanyu.mybatis.session.Configuration;
import com.huanyu.mybatis.type.TypeHandler;
import com.huanyu.mybatis.type.TypeHandlerRegistry;

/**
 * ClassName: ParameterMapping
 * Package: com.huanyu.mybatis.mapping
 * Description: 参数映射 #{property,javaType=int,jdbcType=NUMERIC}
 * SQL语句中的#{}详细应该是这样的：
 * #｛ id, javaType= int, jdbcType=NUMERIC, typeHandler=DemoTypeHandler ｝
 * 该对象就用来存储这个内容
 * 但是对于大多数情况，只有 property=id这一项。其他的会被推测出来，而不是被指定
 * @Author: 寰宇
 * @Create: 2024/6/13 16:07
 * @Version: 1.0
 */
public class ParameterMapping {

    private Configuration configuration;

    // 参数的名称
    private String property;

    // 参数的java类型
    private Class<?> javaType = Object.class;

    // 参数的jdbc类型
    private JdbcType jdbcType;

    private TypeHandler<?> typeHandler;

    private ParameterMapping() {
    }

    public static class Builder {

        // 创建一个新的ParameterMapping实例，用于存储构建过程中的属性值
        private ParameterMapping parameterMapping = new ParameterMapping();

        // 构造方法，初始化ParameterMapping的基本属性
        public Builder(Configuration configuration, String property, Class<?> javaType) {
            // 设置Configuration属性
            parameterMapping.configuration = configuration;
            // 设置参数的属性名
            parameterMapping.property = property;
            // 设置参数的Java类型
            parameterMapping.javaType = javaType;
        }

        // 设置Java类型的方法
        public Builder javaType(Class<?> javaType) {
            parameterMapping.javaType = javaType;
            return this; // 返回Builder实例，以便链式调用
        }

        // 设置JDBC类型的方法
        public Builder jdbcType(JdbcType jdbcType) {
            parameterMapping.jdbcType = jdbcType;
            return this; // 返回Builder实例，以便链式调用
        }

        // 构建ParameterMapping对象的方法
        public ParameterMapping build() {
            // 如果typeHandler未设置且javaType不为空，则自动选择适当的typeHandler
            if (parameterMapping.typeHandler == null && parameterMapping.javaType != null) {
                Configuration configuration = parameterMapping.configuration;
                TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
                // 获取适用于当前javaType和jdbcType的typeHandler
                parameterMapping.typeHandler = typeHandlerRegistry.getTypeHandler(parameterMapping.javaType, parameterMapping.jdbcType);
            }

            // 返回构建完成的ParameterMapping对象
            return parameterMapping;
        }
    }


    public Configuration getConfiguration() {
        return configuration;
    }

    public String getProperty() {
        return property;
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public JdbcType getJdbcType() {
        return jdbcType;
    }

    public TypeHandler<?> getTypeHandler() {
        return typeHandler;
    }
}
