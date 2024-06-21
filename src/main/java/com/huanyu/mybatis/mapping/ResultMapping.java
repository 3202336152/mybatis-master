package com.huanyu.mybatis.mapping;

import com.huanyu.mybatis.session.Configuration;
import com.huanyu.mybatis.type.JdbcType;
import com.huanyu.mybatis.type.TypeHandler;

/**
 * ClassName: ResultMapping
 * Package: com.huanyu.mybatis.mapping
 * Description: 结果映射
 *
 * @Author: 寰宇
 * @Create: 2024/6/20 16:29
 * @Version: 1.0
 */
public class ResultMapping {

    private Configuration configuration;
    private String property;
    private String column;
    private Class<?> javaType;
    private JdbcType jdbcType;
    private TypeHandler<?> typeHandler;

    ResultMapping() {
    }

    public static class Builder {
        private ResultMapping resultMapping = new ResultMapping();


    }
}
