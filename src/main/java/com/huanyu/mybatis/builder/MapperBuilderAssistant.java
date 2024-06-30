package com.huanyu.mybatis.builder;

import com.huanyu.mybatis.cache.Cache;
import com.huanyu.mybatis.cache.decorators.FifoCache;
import com.huanyu.mybatis.cache.impl.PerpetualCache;
import com.huanyu.mybatis.mapping.*;
import com.huanyu.mybatis.reflection.MetaClass;
import com.huanyu.mybatis.scripting.LanguageDriver;
import com.huanyu.mybatis.session.Configuration;
import com.huanyu.mybatis.type.TypeHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
    // 当前Mapper的缓存
    private Cache currentCache;

    public MapperBuilderAssistant(Configuration configuration, String resource) {
        super(configuration);
        this.resource = resource;
    }

    // 构建并返回一个 ResultMapping 对象
    public ResultMapping buildResultMapping(
            Class<?> resultType, // 结果类型
            String property, // 属性名
            String column, // 列名
            List<ResultFlag> flags // 标志列表
    ) {
        // 解析结果的 Java 类型
        Class<?> javaTypeClass = resolveResultJavaType(resultType, property, null);
        // 解析类型处理器实例
        TypeHandler<?> typeHandlerInstance = resolveTypeHandler(javaTypeClass, null);

        // 创建 ResultMapping.Builder 实例
        ResultMapping.Builder builder = new ResultMapping.Builder(configuration, property, column, javaTypeClass);
        builder.typeHandler(typeHandlerInstance); // 设置类型处理器
        builder.flags(flags); // 设置标志

        return builder.build(); // 构建并返回 ResultMapping 对象
    }

    // 解析结果 Java 类型
    private Class<?> resolveResultJavaType(Class<?> resultType, String property, Class<?> javaType) {
        if (javaType == null && property != null) { // 如果 javaType 为空且属性名不为空
            try {
                MetaClass metaResultType = MetaClass.forClass(resultType); // 获取 MetaClass 实例
                javaType = metaResultType.getSetterType(property); // 获取属性的 setter 类型
            } catch (Exception ignore) { // 忽略异常
            }
        }
        if (javaType == null) { // 如果 javaType 仍为空
            javaType = Object.class; // 默认设置为 Object 类型
        }
        return javaType; // 返回解析后的 Java 类型
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
        } else {
            if (base.startsWith(currentNamespace + ".")) {
                return base;
            }
            if (base.contains(".")) {
                throw new RuntimeException("Dots are not allowed in element names, please remove it from " + base);
            }
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
            boolean flushCache,
            boolean useCache,
            LanguageDriver lang
    ) {
        // 给id加上namespace前缀：com.huanyu.mybatis.dao.IUserDao.queryUserInfoById
        id = applyCurrentNamespace(id, false);
        //是否是select语句
        boolean isSelect = sqlCommandType == SqlCommandType.SELECT;

        MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration, id, sqlCommandType, sqlSource, resultType);

        // 结果映射，给 MappedStatement 创建 resultMaps
        setStatementResultMap(resultMap, resultType, statementBuilder);
        setStatementCache(isSelect, flushCache, useCache, currentCache, statementBuilder);

        // 构建mappedStatement
        MappedStatement statement = statementBuilder.build();
        // 映射语句信息，建造完存放到配置项中
        configuration.addMappedStatement(statement);

        return statement;
    }

    private void setStatementCache(
            boolean isSelect,
            boolean flushCache,
            boolean useCache,
            Cache cache,
            MappedStatement.Builder statementBuilder) {
        flushCache = valueOrDefault(flushCache, !isSelect);
        useCache = valueOrDefault(useCache, isSelect);
        statementBuilder.flushCacheRequired(flushCache);
        statementBuilder.useCache(useCache);
        statementBuilder.cache(cache);
    }

    // 创建resultMap 即使不使用resultMap也需要创建，因为resultMap是可以为空的
    private void setStatementResultMap(
            String resultMap,
            Class<?> resultType,
            MappedStatement.Builder statementBuilder) {
        // 因为暂时还没有在 Mapper XML 中配置 Map 返回结果，所以这里返回的是 null
        resultMap = applyCurrentNamespace(resultMap, true);

        List<ResultMap> resultMaps = new ArrayList<>();

        if (resultMap != null) {
            String[] resultMapNames = resultMap.split(",");
            for (String resultMapName : resultMapNames) {
                resultMaps.add(configuration.getResultMap(resultMapName.trim()));
            }
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

    // 构建结果映射
    public ResultMap addResultMap(String id, Class<?> type, List<ResultMapping> resultMappings) {
        // 补全ID全路径，如：com.huanyu.mybatis.dao.IActivityDao + activityMap
        id = applyCurrentNamespace(id, false);
        // 创建 ResultMap.Builder 对象
        ResultMap.Builder inlineResultMapBuilder = new ResultMap.Builder(
                configuration, // 配置对象
                id,            // ResultMap 的标识符
                type,          // 关联的类类型
                resultMappings // ResultMapping 列表
        );
        // 构建 ResultMap 对象
        ResultMap resultMap = inlineResultMapBuilder.build();
        // 将构建好的 ResultMap 添加到配置中
        configuration.addResultMap(resultMap);
        // 返回构建好的 ResultMap
        return resultMap;
    }

    /**
     * 创建一个新的缓存
     * @param typeClass 缓存的实现类
     * @param evictionClass 缓存的清理类，即使用哪种包装类来清理缓存
     * @param flushInterval 缓存清理时间间隔
     * @param size 缓存大小
     * @param readWrite 缓存是否支持读写
     * @param blocking 缓存是否支持阻塞
     * @param props 缓存配置属性
     * @return 缓存
     */
    public Cache useNewCache(Class<? extends Cache> typeClass,
                             Class<? extends Cache> evictionClass,
                             Long flushInterval,
                             Integer size,
                             boolean readWrite,
                             boolean blocking,
                             Properties props) {
        // 判断为null，则用默认值
        typeClass = valueOrDefault(typeClass, PerpetualCache.class);
        evictionClass = valueOrDefault(evictionClass, FifoCache.class);

        // 建造者模式构建 Cache [currentNamespace=com.huanyu.mybatis.dao.IActivityDao]
        Cache cache = new CacheBuilder(currentNamespace)
                .implementation(typeClass)
                .addDecorator(evictionClass)
                .clearInterval(flushInterval)
                .size(size)
                .readWrite(readWrite)
                .blocking(blocking)
                .properties(props)
                .build();

        // 添加缓存
        configuration.addCache(cache);
        currentCache = cache;
        return cache;
    }

    private <T> T valueOrDefault(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

}
