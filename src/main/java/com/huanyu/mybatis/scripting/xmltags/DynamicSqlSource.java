package com.huanyu.mybatis.scripting.xmltags;

import com.huanyu.mybatis.builder.SqlSourceBuilder;
import com.huanyu.mybatis.mapping.BoundSql;
import com.huanyu.mybatis.mapping.SqlSource;
import com.huanyu.mybatis.session.Configuration;

import java.util.Map;

/**
 * ClassName: DynamicSqlSource
 * Package: com.huanyu.mybatis.scripting.xmltags
 * Description: 动态SQL源码
 * SqlSource的重要实现，用以解析动态SQL语句。
 * @Author: 寰宇
 * @Create: 2024/6/26 15:45
 * @Version: 1.0
 */
public class DynamicSqlSource implements SqlSource{

    private Configuration configuration;

    // 整个sqlSource的根节点。
    // 举例子，例如： SELECT *
    //        FROM `user`
    //        WHERE id IN
    //        <foreach item="id" collection="array" open="(" separator="," close=")">
    //            #{id}
    //        </foreach>

    // 那根节点是一个MixedSqlNode，其List分别是：
    // StaticTextSqlNode: SELECT *  FROM `user` WHERE id IN
    // ForEachSqlNode：拆解后的foreachSqlNode信息
    private SqlNode rootSqlNode;

    public DynamicSqlSource(Configuration configuration, SqlNode rootSqlNode) {
        this.configuration = configuration;
        this.rootSqlNode = rootSqlNode;
    }

    /**
     * 获取一个BoundSql对象
     * 关键方法，获取到sql语句中只含有？，并且整理好参数的boundSql
     * @param parameterObject 参数对象
     * @return BoundSql对象
     */
    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        // 生成一个 DynamicContext 动态上下文
        // SQL片段信息
        // 参数信息
        DynamicContext context = new DynamicContext(configuration, parameterObject);
        // 这里会逐层（对于mix的node而言）调用apply。最终不同的节点会调用到不同的apply,完成各自的解析
        // 解析完成的东西拼接到DynamicContext中，里面含有#{}
        // SqlNode.apply 将 ${} 参数替换掉，不替换 #{} 这种参数
        rootSqlNode.apply(context);

        // 调用 SqlSourceBuilder
        // 处理占位符、汇总参数信息
        SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
        Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();

        // SqlSourceBuilder.parse 这里返回的是 StaticSqlSource，解析过程就把那些参数都替换成?了，也就是最基本的JDBC的SQL语句。
        // 使用SqlSourceBuilder处理#{}，将其转化为？
        // 相关参数放进了context.bindings
        // 最终生成了StaticSqlSource对象，然后由它生成BoundSql
        SqlSource sqlSource = sqlSourceParser.parse(context.getSql(), parameterType, context.getBindings());

        // SqlSource.getBoundSql，非递归调用，而是调用 StaticSqlSource 实现类
        BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
        for (Map.Entry<String, Object> entry : context.getBindings().entrySet()) {
            boundSql.setAdditionalParameter(entry.getKey(), entry.getValue());
        }
        return boundSql;
    }
}
