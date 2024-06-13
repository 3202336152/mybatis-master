package com.huanyu.mybatis.mapping;

import java.util.Map;

/**
 * ClassName: BoundSql
 * Package: com.huanyu.mybatis.mapping
 * Description: 绑定的SQL,是从SqlSource而来，将动态内容都处理完成得到的SQL语句字符串，其中包括?,还有绑定的参数
 *
 * @Author: 寰宇
 * @Create: 2024/6/13 16:04
 * @Version: 1.0
 */
public class BoundSql {

    // 可能含有“?”占位符的sql语句
    private String sql;
    // 参数映射列表
    private Map<Integer, String> parameterMappings;
    private String parameterType;

    private String resultType;

    public BoundSql(String sql, Map<Integer, String> parameterMappings, String parameterType, String resultType) {
        this.sql = sql;
        this.parameterMappings = parameterMappings;
        this.parameterType = parameterType;
        this.resultType = resultType;
    }

    public String getSql() {
        return sql;
    }

    public Map<Integer, String> getParameterMappings() {
        return parameterMappings;
    }

    public String getParameterType() {
        return parameterType;
    }

    public String getResultType() {
        return resultType;
    }
}
