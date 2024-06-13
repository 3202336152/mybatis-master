package com.huanyu.mybatis.mapping;

import cn.hutool.db.meta.JdbcType;
import com.huanyu.mybatis.session.Configuration;

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

    private ParameterMapping() {
    }

    public static class Builder {

        private ParameterMapping parameterMapping = new ParameterMapping();

        public Builder(Configuration configuration, String property) {
            parameterMapping.configuration = configuration;
            parameterMapping.property = property;
        }

        public Builder javaType(Class<?> javaType) {
            parameterMapping.javaType = javaType;
            return this;
        }

        public Builder jdbcType(JdbcType jdbcType) {
            parameterMapping.jdbcType = jdbcType;
            return this;
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
}
