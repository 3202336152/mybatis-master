package com.huanyu.mybatis.session;

import com.huanyu.mybatis.binding.MapperRegistry;
import com.huanyu.mybatis.datasource.druid.DruidDataSourceFactory;
import com.huanyu.mybatis.datasource.pooled.PooledDataSourceFactory;
import com.huanyu.mybatis.datasource.unpooled.UnpooledDataSourceFactory;
import com.huanyu.mybatis.executor.Executor;
import com.huanyu.mybatis.executor.SimpleExecutor;
import com.huanyu.mybatis.executor.parameter.ParameterHandler;
import com.huanyu.mybatis.executor.resultset.DefaultResultSetHandler;
import com.huanyu.mybatis.executor.resultset.ResultSetHandler;
import com.huanyu.mybatis.executor.statement.PreparedStatementHandler;
import com.huanyu.mybatis.executor.statement.StatementHandler;
import com.huanyu.mybatis.mapping.BoundSql;
import com.huanyu.mybatis.mapping.MappedStatement;
import com.huanyu.mybatis.mapping.Environment;
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

    /**
     * 环境
     */
    protected Environment environment;

    /**
     * 映射注册机
     */
    protected MapperRegistry mapperRegistry = new MapperRegistry(this);

    /**
     * 映射的语句，存在Map里
     */
    protected final Map<String, MappedStatement> mappedStatements = new HashMap<>();

    /**
     * 类型别名注册机
     */
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
    public ResultSetHandler newResultSetHandler(Executor executor, MappedStatement mappedStatement, BoundSql boundSql) {
        return new DefaultResultSetHandler(executor, mappedStatement, boundSql);
    }

    /**
     * 生产执行器
     */
    public Executor newExecutor(Transaction transaction) {
        return new SimpleExecutor(this, transaction);
    }

    /**
     * 创建语句处理器
     */
    public StatementHandler newStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameter, ResultHandler resultHandler, BoundSql boundSql) {
        return new PreparedStatementHandler(executor, mappedStatement, parameter, resultHandler, boundSql);
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


}
