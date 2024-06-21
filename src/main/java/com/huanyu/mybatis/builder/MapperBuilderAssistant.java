package com.huanyu.mybatis.builder;

import com.huanyu.mybatis.mapping.MappedStatement;
import com.huanyu.mybatis.mapping.ResultMap;
import com.huanyu.mybatis.mapping.SqlCommandType;
import com.huanyu.mybatis.mapping.SqlSource;
import com.huanyu.mybatis.scripting.LanguageDriver;
import com.huanyu.mybatis.session.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: MapperBuilderAssistant
 * Package: com.huanyu.mybatis.builder
 * Description: 映射构建器助手，建造者
 *
 * @Author: 寰宇
 * @Create: 2024/6/20 16:35
 * @Version: 1.0
 */
public class MapperBuilderAssistant extends BaseBuilder{
    // 当前Mapper接口的命名空间
    private String currentNamespace;
    // Mapper接口文件的路径
    private String resource;

    public MapperBuilderAssistant(Configuration configuration, String resource) {
        super(configuration);
        this.resource = resource;
    }

    public String getCurrentNamespace() {
        return currentNamespace;
    }

    /**
     * 设置当前的namespace,只允许设置一次不允许修改
     * @param currentNamespace 当前的namespace
     */
    public void setCurrentNamespace(String currentNamespace) {
        this.currentNamespace = currentNamespace;
    }

    /**
     * 使用当前的命名空间来确定base的命名空间
     * @param base 一个路径
     * @param isReference 是否参考当前命名空间
     * @return 在当前命名空间基础上的路径
     */
    public String applyCurrentNamespace(String base, boolean isReference) {
        if (base == null) {
            return null;
        }
        if (isReference) {
            if (base.contains(".")) return base;
        }
        return currentNamespace + "." + base;
    }

    /**
     * 添加映射器语句
     */
    public MappedStatement addMappedStatement(
            String id,
            SqlSource sqlSource,
            SqlCommandType sqlCommandType,
            Class<?> parameterType,
            String resultMap,
            Class<?> resultType,
            LanguageDriver lang
    ) {
        // 给id加上namespace前缀：com.huanyu.mybatis.dao.IUserDao.queryUserInfoById
        id = applyCurrentNamespace(id, false);
        MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration, id, sqlCommandType, sqlSource, resultType);

        // 结果映射，给 MappedStatement#resultMaps
        setStatementResultMap(resultMap, resultType, statementBuilder);

        MappedStatement statement = statementBuilder.build();
        // 映射语句信息，建造完存放到配置项中
        configuration.addMappedStatement(statement);

        return statement;
    }

    private void setStatementResultMap(
            String resultMap,
            Class<?> resultType,
            MappedStatement.Builder statementBuilder) {
        // 因为暂时还没有在 Mapper XML 中配置 Map 返回结果，所以这里返回的是 null
        resultMap = applyCurrentNamespace(resultMap, true);

        List<ResultMap> resultMaps = new ArrayList<>();

        if (resultMap != null) {
            // TODO：暂无Map结果映射配置，本章节不添加此逻辑
        }
        /*
         * 通常使用 resultType 即可满足大部分场景
         * <select id="queryUserInfoById" resultType="com.huanyu.mybatis.po.User">
         * 使用 resultType 的情况下，Mybatis 会自动创建一个 ResultMap，基于属性名称映射列到 JavaBean 的属性上。
         */
        else if (resultType != null) {
            ResultMap.Builder inlineResultMapBuilder = new ResultMap.Builder(
                    configuration,
                    statementBuilder.id() + "-Inline",
                    resultType,
                    new ArrayList<>());
            resultMaps.add(inlineResultMapBuilder.build());
        }
        statementBuilder.resultMaps(resultMaps);
    }
}
