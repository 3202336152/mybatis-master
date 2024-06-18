package com.huanyu.mybatis.scripting.defaults;

import com.huanyu.mybatis.builder.SqlSourceBuilder;
import com.huanyu.mybatis.mapping.BoundSql;
import com.huanyu.mybatis.mapping.SqlSource;
import com.huanyu.mybatis.scripting.xmltags.DynamicContext;
import com.huanyu.mybatis.scripting.xmltags.SqlNode;
import com.huanyu.mybatis.session.Configuration;

import java.util.HashMap;

/**
 * ClassName: RawSqlSource
 * Package: com.huanyu.mybatis.scripting.defaults
 * Description: 原始SQL源码，比 DynamicSqlSource 动态SQL处理快
 *
 * @Author: 寰宇
 * @Create: 2024/6/17 21:46
 * @Version: 1.0
 */
public class RawSqlSource implements SqlSource {

    // StaticSqlSource对象
    private final SqlSource sqlSource;

    public RawSqlSource(Configuration configuration, SqlNode rootSqlNode, Class<?> parameterType) {
        this(configuration, getSql(configuration, rootSqlNode), parameterType);
    }

    public RawSqlSource(Configuration configuration, String sql, Class<?> parameterType) {
        SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
        Class<?> clazz = parameterType == null ? Object.class : parameterType;
        // 处理RawSqlSource中的“#{}”占位符，得到StaticSqlSource
        sqlSource = sqlSourceParser.parse(sql, clazz, new HashMap<>());
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        // BoundSql对象由sqlSource属性持有的StaticSqlSource对象返回
        return sqlSource.getBoundSql(parameterObject);
    }

    private static String getSql(Configuration configuration, SqlNode rootSqlNode) {
        DynamicContext context = new DynamicContext(configuration, null);
        rootSqlNode.apply(context);
        return context.getSql();
    }
}
