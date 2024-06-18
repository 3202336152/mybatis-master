package com.huanyu.mybatis.builder;

import com.huanyu.mybatis.mapping.BoundSql;
import com.huanyu.mybatis.mapping.ParameterMapping;
import com.huanyu.mybatis.mapping.SqlSource;
import com.huanyu.mybatis.session.Configuration;

import java.util.List;

/**
 * ClassName: StaticSqlSource
 * Package: com.huanyu.mybatis.builder
 * Description: 静态SQL源码
 *
 * @Author: 寰宇
 * @Create: 2024/6/17 21:32
 * @Version: 1.0
 */
public class StaticSqlSource implements SqlSource {

    // 经过解析后，不存在${}和#{}这两种符号，只剩下?符号的SQL语句
    private String sql;
    // SQL语句对应的参数列表
    private List<ParameterMapping> parameterMappings;
    // 配置信息
    private Configuration configuration;

    public StaticSqlSource(Configuration configuration, String sql) {
        this(configuration, sql, null);
    }

    public StaticSqlSource(Configuration configuration, String sql, List<ParameterMapping> parameterMappings) {
        this.sql = sql;
        this.parameterMappings = parameterMappings;
        this.configuration = configuration;
    }

    /**
     * 组建一个BoundSql对象
     * @param parameterObject 参数对象
     * @return 组件的BoundSql对象
     */
    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        return new BoundSql(configuration, sql, parameterMappings, parameterObject);
    }
}
