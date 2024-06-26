package com.huanyu.mybatis.session;

import com.huanyu.mybatis.binding.MapperRegistry;
import com.huanyu.mybatis.cache.Cache;
import com.huanyu.mybatis.cache.decorators.FifoCache;
import com.huanyu.mybatis.cache.impl.PerpetualCache;
import com.huanyu.mybatis.datasource.druid.DruidDataSourceFactory;
import com.huanyu.mybatis.datasource.pooled.PooledDataSourceFactory;
import com.huanyu.mybatis.datasource.unpooled.UnpooledDataSourceFactory;
import com.huanyu.mybatis.executor.CachingExecutor;
import com.huanyu.mybatis.executor.Executor;
import com.huanyu.mybatis.executor.SimpleExecutor;
import com.huanyu.mybatis.executor.parameter.ParameterHandler;
import com.huanyu.mybatis.executor.resultset.DefaultResultSetHandler;
import com.huanyu.mybatis.executor.resultset.ResultSetHandler;
import com.huanyu.mybatis.executor.statement.PreparedStatementHandler;
import com.huanyu.mybatis.executor.statement.StatementHandler;
import com.huanyu.mybatis.mapping.BoundSql;
import com.huanyu.mybatis.mapping.Environment;
import com.huanyu.mybatis.mapping.MappedStatement;
import com.huanyu.mybatis.mapping.ResultMap;
import com.huanyu.mybatis.plugin.Interceptor;
import com.huanyu.mybatis.plugin.InterceptorChain;
import com.huanyu.mybatis.reflection.MetaObject;
import com.huanyu.mybatis.reflection.factory.DefaultObjectFactory;
import com.huanyu.mybatis.reflection.factory.ObjectFactory;
import com.huanyu.mybatis.reflection.wrapper.DefaultObjectWrapperFactory;
import com.huanyu.mybatis.reflection.wrapper.ObjectWrapperFactory;
import com.huanyu.mybatis.scripting.LanguageDriver;
import com.huanyu.mybatis.scripting.LanguageDriverRegistry;
import com.huanyu.mybatis.scripting.xmltags.XMLLanguageDriver;
import com.huanyu.mybatis.transaction.Transaction;
import com.huanyu.mybatis.transaction.jdbc.JdbcTransactionFactory;
import com.huanyu.mybatis.type.TypeAliasRegistry;
import com.huanyu.mybatis.type.TypeHandlerRegistry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * ClassName: Configuration
 * Package: com.huanyu.mybatis.session
 * Description: 配置项
 * 主要内容分为以下几个部分：
 * 1、大量的配置项，和与`<configuration>`标签中的配置对应
 * 2、创建类型别名注册机，并向内注册了大量的类型别名
 * 3、创建了大量Map，包括存储映射语句的Map，存储缓存的Map等，这些Map使用的是一种不允许覆盖的严格Map
 * 4、给出了大量的处理器的创建方法，包括参数处理器、语句处理器、结果处理器、执行器。
 * 这里并没有真正创建，只是给出了方法。
 *
 * @Author: 寰宇
 * @Create: 2024/6/12 14:37
 * @Version: 1.0
 */
public class Configuration {

    // 环境
    protected Environment environment;

   // 映射注册机
    protected MapperRegistry mapperRegistry = new MapperRegistry(this);

    // 默认启用缓存，cacheEnabled = true/false
    protected boolean cacheEnabled = true;
    // 缓存机制，默认不配置的情况是 SESSION
    protected LocalCacheScope localCacheScope = LocalCacheScope.SESSION;

    // 映射的语句，存在Map里
    protected final Map<String, MappedStatement> mappedStatements = new HashMap<>();

    // 缓存,存在Map里
    protected final Map<String, Cache> caches = new HashMap<>();

    // 结果映射，存在Map里
    protected final Map<String, ResultMap> resultMaps = new HashMap<>();

    // 插件拦截器链
    protected final InterceptorChain interceptorChain = new InterceptorChain();

    // 类型别名注册机
    protected final TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();
    protected final LanguageDriverRegistry languageRegistry = new LanguageDriverRegistry();

    // 类型处理器注册机
    protected final TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();

    // 对象工厂和对象包装器工厂
    protected ObjectFactory objectFactory = new DefaultObjectFactory();
    protected ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();

    protected final Set<String> loadedResources = new HashSet<>();

    protected String databaseId;

    public Configuration() {
        typeAliasRegistry.registerAlias("JDBC", JdbcTransactionFactory.class);

        typeAliasRegistry.registerAlias("DRUID", DruidDataSourceFactory.class);
        typeAliasRegistry.registerAlias("UNPOOLED", UnpooledDataSourceFactory.class);
        typeAliasRegistry.registerAlias("POOLED", PooledDataSourceFactory.class);

        typeAliasRegistry.registerAlias("PERPETUAL", PerpetualCache.class);
        typeAliasRegistry.registerAlias("FIFO", FifoCache.class);

        languageRegistry.setDefaultDriverClass(XMLLanguageDriver.class);
    }

    public void addMappers(String packageName) {
        mapperRegistry.addMappers(packageName);
    }

    public <T> void addMapper(Class<T> type) {
        mapperRegistry.addMapper(type);
    }

    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        return mapperRegistry.getMapper(type, sqlSession);
    }

    public boolean hasMapper(Class<?> type) {
        return mapperRegistry.hasMapper(type);
    }

    public void addMappedStatement(MappedStatement ms) {
        mappedStatements.put(ms.getId(), ms);
    }

    public MappedStatement getMappedStatement(String id) {
        return mappedStatements.get(id);
    }

    public TypeAliasRegistry getTypeAliasRegistry() {
        return typeAliasRegistry;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public String getDatabaseId() {
        return databaseId;
    }

    /**
     * 创建结果集处理器
     */
    public ResultSetHandler newResultSetHandler(Executor executor, MappedStatement mappedStatement, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        return new DefaultResultSetHandler(executor, mappedStatement, resultHandler, rowBounds, boundSql);
    }

    /**
     * 生产执行器
     */
    public Executor newExecutor(Transaction transaction) {
        Executor executor = new SimpleExecutor(this, transaction);
        // 配置开启缓存，创建 CachingExecutor(默认就是有缓存)装饰者模式
        if (cacheEnabled) {
            executor = new CachingExecutor(executor); // 具体装饰类
        }
        return executor;
    }

    /**
     * 创建语句处理器
     */
    public StatementHandler newStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        // 创建语句处理器，Mybatis 这里加了路由 STATEMENT、PREPARED、CALLABLE 我们默认只根据预处理进行实例化
        StatementHandler statementHandler = new PreparedStatementHandler(executor, mappedStatement, parameter, rowBounds, resultHandler, boundSql);
        // 嵌入插件，代理对象
        statementHandler = (StatementHandler) interceptorChain.pluginAll(statementHandler);
        return statementHandler;
    }

    // 创建元对象
    public MetaObject newMetaObject(Object object) {
        return MetaObject.forObject(object, objectFactory, objectWrapperFactory);
    }

    // 类型处理器注册机
    public TypeHandlerRegistry getTypeHandlerRegistry() {
        return typeHandlerRegistry;
    }


    public boolean isResourceLoaded(String resource) {
        return loadedResources.contains(resource);
    }

    public void addLoadedResource(String resource) {
        loadedResources.add(resource);
    }

    public LanguageDriverRegistry getLanguageRegistry() {
        return languageRegistry;
    }

    public ParameterHandler newParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        // 创建参数处理器
        ParameterHandler parameterHandler = mappedStatement.getLang().createParameterHandler(mappedStatement, parameterObject, boundSql);
        // 插件的一些参数，也是在这里处理，暂时不添加这部分内容 interceptorChain.pluginAll(parameterHandler);
        return parameterHandler;
    }

    public LanguageDriver getDefaultScriptingLanguageInstance() {
        return languageRegistry.getDefaultDriver();
    }

    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }

    public ResultMap getResultMap(String id) {
        return resultMaps.get(id);
    }

    public void addResultMap(ResultMap resultMap) {
        resultMaps.put(resultMap.getId(), resultMap);
    }

    public void addInterceptor(Interceptor interceptorInstance) {
        interceptorChain.addInterceptor(interceptorInstance);
    }

    public LocalCacheScope getLocalCacheScope() {
        return localCacheScope;
    }

    public void setLocalCacheScope(LocalCacheScope localCacheScope) {
        this.localCacheScope = localCacheScope;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public void addCache(Cache cache) {
        caches.put(cache.getId(), cache);
    }

    public Cache getCache(String id) {
        return caches.get(id);
    }

}
