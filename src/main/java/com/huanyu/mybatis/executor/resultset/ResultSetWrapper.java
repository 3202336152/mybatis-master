package com.huanyu.mybatis.executor.resultset;

import com.huanyu.mybatis.io.Resources;
import com.huanyu.mybatis.mapping.ResultMap;
import com.huanyu.mybatis.session.Configuration;
import com.huanyu.mybatis.type.JdbcType;
import com.huanyu.mybatis.type.TypeHandler;
import com.huanyu.mybatis.type.TypeHandlerRegistry;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 * ClassName: ResultSetWrapper
 * Package: com.huanyu.mybatis.executor.resultset
 * Description: 结果集包装器
 *
 * @Author: 寰宇
 * @Create: 2024/6/20 18:40
 * @Version: 1.0
 */
public class ResultSetWrapper {

    // 包装的 ResultSet 对象
    private final ResultSet resultSet;
    // TypeHandlerRegistry 对象，用于获取 TypeHandler
    private final TypeHandlerRegistry typeHandlerRegistry;
    // 存储列名的列表
    private final List<String> columnNames = new ArrayList<>();
    // 存储列对应的类名的列表
    private final List<String> classNames = new ArrayList<>();
    // 存储列对应的 JDBC 类型的列表
    private final List<JdbcType> jdbcTypes = new ArrayList<>();
    // 存储列名和对应的 TypeHandler 的映射
    private final Map<String, Map<Class<?>, TypeHandler<?>>> typeHandlerMap = new HashMap<>();
    // 存储结果映射和列前缀对应的已映射列名的映射
    private Map<String, List<String>> mappedColumnNamesMap = new HashMap<>();
    // 存储结果映射和列前缀对应的未映射列名的映射
    private Map<String, List<String>> unMappedColumnNamesMap = new HashMap<>();

    // 构造函数，初始化 ResultSetWrapper
    public ResultSetWrapper(ResultSet rs, Configuration configuration) throws SQLException {
        super();
        this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        this.resultSet = rs;
        final ResultSetMetaData metaData = rs.getMetaData();
        final int columnCount = metaData.getColumnCount();
        // 遍历所有列，获取列名、JDBC 类型和类名，并添加到相应的列表中
        for (int i = 1; i <= columnCount; i++) {
            columnNames.add(metaData.getColumnLabel(i));
            jdbcTypes.add(JdbcType.forCode(metaData.getColumnType(i)));
            classNames.add(metaData.getColumnClassName(i));
        }
    }

    // 获取 ResultSet 对象
    public ResultSet getResultSet() {
        return resultSet;
    }

    // 获取列名列表
    public List<String> getColumnNames() {
        return this.columnNames;
    }

    // 获取类名列表
    public List<String> getClassNames() {
        return Collections.unmodifiableList(classNames);
    }

    // 获取列对应的 TypeHandler
    public TypeHandler<?> getTypeHandler(Class<?> propertyType, String columnName) {
        TypeHandler<?> handler = null;
        // 获取列名对应的 TypeHandler 映射
        Map<Class<?>, TypeHandler<?>> columnHandlers = typeHandlerMap.get(columnName);
        if (columnHandlers == null) {
            columnHandlers = new HashMap<>();
            typeHandlerMap.put(columnName, columnHandlers);
        } else {
            handler = columnHandlers.get(propertyType);
        }
        if (handler == null) {
            // 从注册表中获取 TypeHandler
            handler = typeHandlerRegistry.getTypeHandler(propertyType, null);
            columnHandlers.put(propertyType, handler);
        }
        return handler;
    }

    // 解析类名，返回对应的 Class 对象
    private Class<?> resolveClass(String className) {
        try {
            return Resources.classForName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    // 加载已映射和未映射的列名
    private void loadMappedAndUnmappedColumnNames(ResultMap resultMap, String columnPrefix) throws SQLException {
        // 用于存储已映射的列名
        List<String> mappedColumnNames = new ArrayList<String>();
        // 用于存储未映射的列名
        List<String> unmappedColumnNames = new ArrayList<String>();
        // 将列名前缀转换为大写
        final String upperColumnPrefix = columnPrefix == null ? null : columnPrefix.toUpperCase(Locale.ENGLISH);
        // 获取已映射的列名集合，并加上前缀
        final Set<String> mappedColumns = prependPrefixes(resultMap.getMappedColumns(), upperColumnPrefix);

        // 遍历所有列名，将列名分类到已映射和未映射的列表中
        for (String columnName : columnNames) {
            // 将列名转换为大写
            final String upperColumnName = columnName.toUpperCase(Locale.ENGLISH);
            // 判断该列是否在已映射的列名集合中
            if (mappedColumns.contains(upperColumnName)) {
                // 如果在，则添加到已映射的列名列表中
                mappedColumnNames.add(upperColumnName);
            } else {
                // 否则，添加到未映射的列名列表中
                unmappedColumnNames.add(columnName);
            }
        }
        // 将结果存储到相应的映射中，以便后续快速访问
        mappedColumnNamesMap.put(getMapKey(resultMap, columnPrefix), mappedColumnNames);
        unMappedColumnNamesMap.put(getMapKey(resultMap, columnPrefix), unmappedColumnNames);
    }

    // 获取已映射的列名
    public List<String> getMappedColumnNames(ResultMap resultMap, String columnPrefix) throws SQLException {
        // 获取已映射的列名列表
        List<String> mappedColumnNames = mappedColumnNamesMap.get(getMapKey(resultMap, columnPrefix));
        // 如果映射不存在，则加载映射
        if (mappedColumnNames == null) {
            loadMappedAndUnmappedColumnNames(resultMap, columnPrefix);
            mappedColumnNames = mappedColumnNamesMap.get(getMapKey(resultMap, columnPrefix));
        }
        // 返回已映射的列名列表
        return mappedColumnNames;
    }

    // 获取未映射的列名
    public List<String> getUnmappedColumnNames(ResultMap resultMap, String columnPrefix) throws SQLException {
        // 获取未映射的列名列表
        List<String> unMappedColumnNames = unMappedColumnNamesMap.get(getMapKey(resultMap, columnPrefix));
        // 如果映射不存在，则加载映射
        if (unMappedColumnNames == null) {
            loadMappedAndUnmappedColumnNames(resultMap, columnPrefix);
            unMappedColumnNames = unMappedColumnNamesMap.get(getMapKey(resultMap, columnPrefix));
        }

        // 返回未映射的列名列表
        return unMappedColumnNames;
    }


    // 获取用于映射的键
    private String getMapKey(ResultMap resultMap, String columnPrefix) {
        return resultMap.getId() + ":" + columnPrefix;
    }

    // 添加前缀到列名前
    private Set<String> prependPrefixes(Set<String> columnNames, String prefix) {
        if (columnNames == null || columnNames.isEmpty() || prefix == null || prefix.length() == 0) {
            return columnNames;
        }
        final Set<String> prefixed = new HashSet<String>();
        for (String columnName : columnNames) {
            prefixed.add(prefix + columnName);
        }
        return prefixed;
    }
}
