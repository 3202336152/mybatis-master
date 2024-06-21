package com.huanyu.mybatis.executor.resultset;

import com.huanyu.mybatis.executor.Executor;
import com.huanyu.mybatis.executor.result.DefaultResultContext;
import com.huanyu.mybatis.executor.result.DefaultResultHandler;
import com.huanyu.mybatis.mapping.BoundSql;
import com.huanyu.mybatis.mapping.MappedStatement;
import com.huanyu.mybatis.mapping.ResultMap;
import com.huanyu.mybatis.mapping.ResultMapping;
import com.huanyu.mybatis.reflection.MetaClass;
import com.huanyu.mybatis.reflection.MetaObject;
import com.huanyu.mybatis.reflection.factory.ObjectFactory;
import com.huanyu.mybatis.session.Configuration;
import com.huanyu.mybatis.session.ResultHandler;
import com.huanyu.mybatis.session.RowBounds;
import com.huanyu.mybatis.type.TypeHandler;
import com.huanyu.mybatis.type.TypeHandlerRegistry;

import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * ClassName: DefaultResultSetHandler
 * Package: com.huanyu.mybatis.executor.resultset
 * Description: 默认Map结果处理器
 *
 * @Author: 寰宇
 * @Create: 2024/6/15 16:20
 * @Version: 1.0
 */
public class DefaultResultSetHandler implements ResultSetHandler{

    private final Configuration configuration;
    private final MappedStatement mappedStatement;
    private final RowBounds rowBounds;
    private final ResultHandler resultHandler;
    private final BoundSql boundSql;
    private final TypeHandlerRegistry typeHandlerRegistry;
    private final ObjectFactory objectFactory;

    public DefaultResultSetHandler(Executor executor, MappedStatement mappedStatement, ResultHandler resultHandler, RowBounds rowBounds, BoundSql boundSql) {
        this.configuration = mappedStatement.getConfiguration();
        this.rowBounds = rowBounds;
        this.boundSql = boundSql;
        this.mappedStatement = mappedStatement;
        this.resultHandler = resultHandler;
        this.objectFactory = configuration.getObjectFactory();
        this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
    }

    /**
     * 处理Statement得到的多结果集（也可能是单结果集，这是多结果集的一种简化形式），最终得到结果列表
     * @param stmt Statement语句
     * @return 结果列表
     * @throws SQLException
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Object> handleResultSets(Statement stmt) throws SQLException {
        // 创建一个List用于存储多个结果集
        final List<Object> multipleResults = new ArrayList<>();
        // 初始化结果集计数器
        int resultSetCount = 0;
        // 使用ResultSetWrapper包装初始结果集
        ResultSetWrapper rsw = new ResultSetWrapper(stmt.getResultSet(), configuration);
        // 获取映射的结果集
        List<ResultMap> resultMaps = mappedStatement.getResultMaps();
        // 当存在结果集并且结果集映射的数量大于当前结果集计数时，循环处理结果集
        while (rsw != null && resultMaps.size() > resultSetCount) {
            // 获取当前结果集对应的结果映射
            ResultMap resultMap = resultMaps.get(resultSetCount);
            // 处理当前结果集并将结果添加到multipleResults中
            handleResultSet(rsw, resultMap, multipleResults, null);
            // 获取下一个结果集
            rsw = getNextResultSet(stmt);
            // 结果集计数器加一
            resultSetCount++;
        }
        // 如果只有一个结果集，则返回该结果集，否则返回包含所有结果集的列表
        return multipleResults.size() == 1 ? (List<Object>) multipleResults.get(0) : multipleResults;
    }


    // 尝试获取下一个结果集，并用 ResultSetWrapper 包装返回
    private ResultSetWrapper getNextResultSet(Statement stmt) throws SQLException {
        // 使该方法对糟糕的JDBC驱动程序具有容错能力
        try {
            // 检查连接是否支持多个结果集
            if (stmt.getConnection().getMetaData().supportsMultipleResultSets()) {
                // 标准的JDBC方式确定是否有更多结果集
                if (!((!stmt.getMoreResults()) && (stmt.getUpdateCount() == -1))) {
                    // 获取结果集
                    ResultSet rs = stmt.getResultSet();
                    // 如果结果集不为 null，则返回包装后的结果集，否则返回 null
                    return rs != null ? new ResultSetWrapper(rs, configuration) : null;
                }
            }
        } catch (Exception ignore) {
            // 故意忽略异常
        }
        // 如果没有更多结果集，返回 null
        return null;
    }

    // 处理单个结果集并将结果添加到 multipleResults 列表中
    private void handleResultSet(ResultSetWrapper rsw, ResultMap resultMap, List<Object> multipleResults, ResultMapping parentMapping) throws SQLException {
        // 如果 resultHandler 为空，则创建一个新的 DefaultResultHandler
        if (resultHandler == null) {
            // 1. 新创建结果处理器
            DefaultResultHandler defaultResultHandler = new DefaultResultHandler(objectFactory);
            // 2. 封装数据，将结果集中的行映射到结果对象
            handleRowValuesForSimpleResultMap(rsw, resultMap, defaultResultHandler, rowBounds, null);
            // 3. 保存结果，将处理后的结果添加到 multipleResults 列表中
            multipleResults.add(defaultResultHandler.getResultList());
        }
    }

    // 处理结果集中的行，将每行映射到结果对象
    private void handleRowValuesForSimpleResultMap(ResultSetWrapper rsw, ResultMap resultMap, ResultHandler resultHandler, RowBounds rowBounds, ResultMapping parentMapping) throws SQLException {
        // 创建一个 DefaultResultContext 用于记录结果上下文
        DefaultResultContext resultContext = new DefaultResultContext();
        // 遍历结果集中的每一行，直到达到行数限制
        while (resultContext.getResultCount() < rowBounds.getLimit() && rsw.getResultSet().next()) {
            // 获取一行的值
            Object rowValue = getRowValue(rsw, resultMap);
            // 调用 resultHandler 处理这一行的值
            callResultHandler(resultHandler, resultContext, rowValue);
        }
    }

    // 调用 resultHandler 处理结果上下文中的结果对象
    private void callResultHandler(ResultHandler resultHandler, DefaultResultContext resultContext, Object rowValue) {
        // 将结果对象添加到结果上下文中
        resultContext.nextResultObject(rowValue);
        // 使用 resultHandler 处理结果上下文
        resultHandler.handleResult(resultContext);
    }

    /**
     * 获取一行的值，将结果集中的一行映射到结果对象
     */
    private Object getRowValue(ResultSetWrapper rsw, ResultMap resultMap) throws SQLException {
        // 根据结果映射类型，实例化结果对象
        Object resultObject = createResultObject(rsw, resultMap, null);
        // 如果结果对象不为空且没有对应类型的 TypeHandler
        if (resultObject != null && !typeHandlerRegistry.hasTypeHandler(resultMap.getType())) {
            // 创建 MetaObject 用于操作结果对象
            final MetaObject metaObject = configuration.newMetaObject(resultObject);
            // 自动映射结果集中的列到结果对象的属性
            applyAutomaticMappings(rsw, resultMap, metaObject, null);
        }
        // 返回结果对象
        return resultObject;
    }

    // 创建结果对象，根据结果映射类型和列前缀
    private Object createResultObject(ResultSetWrapper rsw, ResultMap resultMap, String columnPrefix) throws SQLException {
        // 创建一个用于存放构造参数类型的列表
        final List<Class<?>> constructorArgTypes = new ArrayList<>();
        // 创建一个用于存放构造参数值的列表
        final List<Object> constructorArgs = new ArrayList<>();
        // 调用重载的 createResultObject 方法创建结果对象
        return createResultObject(rsw, resultMap, constructorArgTypes, constructorArgs, columnPrefix);
    }

    /**
     * 创建结果对象
     */
    private Object createResultObject(ResultSetWrapper rsw, ResultMap resultMap, List<Class<?>> constructorArgTypes, List<Object> constructorArgs, String columnPrefix) throws SQLException {
        // 获取结果映射类型
        final Class<?> resultType = resultMap.getType();
        // 创建 MetaClass 用于操作结果类型
        final MetaClass metaType = MetaClass.forClass(resultType);
        // 如果结果类型是接口或有默认构造函数
        if (resultType.isInterface() || metaType.hasDefaultConstructor()) {
            // 创建并返回结果对象
            return objectFactory.create(resultType);
        }
        // 如果无法创建结果对象，抛出异常
        throw new RuntimeException("Do not know how to create an instance of " + resultType);
    }

    // 将结果集中的列映射到结果对象的属性上
    private boolean applyAutomaticMappings(ResultSetWrapper rsw, ResultMap resultMap, MetaObject metaObject, String columnPrefix) throws SQLException {
        // 获取在resultMap中未映射的列名列表
        final List<String> unmappedColumnNames = rsw.getUnmappedColumnNames(resultMap, columnPrefix);
        boolean foundValues = false; // 标记是否找到了映射的值
        // 遍历所有未映射的列名
        for (String columnName : unmappedColumnNames) {
            String propertyName = columnName; // 默认属性名与列名相同

            // 如果指定了columnPrefix，处理列名前缀
            if (columnPrefix != null && !columnPrefix.isEmpty()) {
                // 当指定了columnPrefix时，忽略没有该前缀的列
                if (columnName.toUpperCase(Locale.ENGLISH).startsWith(columnPrefix)) {
                    propertyName = columnName.substring(columnPrefix.length()); // 去掉前缀
                } else {
                    continue; // 跳过不符合前缀的列
                }
            }
            // 在MetaObject中查找对应的属性名
            final String property = metaObject.findProperty(propertyName, false);
            // 如果属性存在且有对应的setter方法
            if (property != null && metaObject.hasSetter(property)) {
                // 获取属性的类型
                final Class<?> propertyType = metaObject.getSetterType(property);
                // 如果有对应类型的TypeHandler
                if (typeHandlerRegistry.hasTypeHandler(propertyType)) {
                    // 获取TypeHandler来处理这个属性类型的结果
                    final TypeHandler<?> typeHandler = rsw.getTypeHandler(propertyType, columnName);
                    // 使用TypeHandler从ResultSet中获取值
                    final Object value = typeHandler.getResult(rsw.getResultSet(), columnName);
                    // 如果成功获取到值，则标记foundValues为true
                    if (value != null) {
                        foundValues = true;
                    }
                    // 如果值非空或者属性类型不是基本类型，将值设置到metaObject中
                    if (value != null || !propertyType.isPrimitive()) {
                        // 通过反射工具类设置属性值
                        metaObject.setValue(property, value);
                    }
                }
            }
        }
        // 返回是否找到了映射的值
        return foundValues;
    }

}
