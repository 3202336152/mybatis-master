package com.huanyu.mybatis.scripting.defaults;

import com.huanyu.mybatis.type.JdbcType;
import com.alibaba.fastjson.JSON;
import com.huanyu.mybatis.executor.parameter.ParameterHandler;
import com.huanyu.mybatis.mapping.BoundSql;
import com.huanyu.mybatis.mapping.MappedStatement;
import com.huanyu.mybatis.mapping.ParameterMapping;
import com.huanyu.mybatis.reflection.MetaObject;
import com.huanyu.mybatis.session.Configuration;
import com.huanyu.mybatis.type.TypeHandler;
import com.huanyu.mybatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * ClassName: DefaultParameterHandler
 * Package: com.huanyu.mybatis.scripting.defaults
 * Description: 默认参数处理器
 *
 * @Author: 寰宇
 * @Create: 2024/6/19 14:53
 * @Version: 1.0
 */
public class DefaultParameterHandler implements ParameterHandler {

    private Logger logger = LoggerFactory.getLogger(DefaultParameterHandler.class);

    private final TypeHandlerRegistry typeHandlerRegistry;

    private final MappedStatement mappedStatement;
    private final Object parameterObject;
    private BoundSql boundSql;
    private Configuration configuration;

    public DefaultParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        this.mappedStatement = mappedStatement;
        this.configuration = mappedStatement.getConfiguration();
        this.typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();
        this.parameterObject = parameterObject;
        this.boundSql = boundSql;
    }

    @Override
    public Object getParameterObject() {
        return parameterObject;
    }

    @Override
    public void setParameters(PreparedStatement ps) throws SQLException {
        // 获取绑定的SQL语句中的参数映射列表
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        // 如果参数映射列表不为空
        if (null != parameterMappings) {
            // 遍历每一个参数映射
            for (int i = 0; i < parameterMappings.size(); i++) {
                ParameterMapping parameterMapping = parameterMappings.get(i);
                // 获取当前参数映射的属性名
                String propertyName = parameterMapping.getProperty();
                Object value;
                // 判断是否有类型处理器用于处理当前参数对象的类型
                if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                    // 如果有类型处理器，则直接使用参数对象作为值
                    value = parameterObject;
                } else {
                    // 否则，通过 MetaObject 反射获取参数对象的属性值
                    MetaObject metaObject = configuration.newMetaObject(parameterObject);
                    value = metaObject.getValue(propertyName);
                }
                // 获取参数映射中的 JDBC 类型
                JdbcType jdbcType = parameterMapping.getJdbcType();

                // 设置参数
                // 记录日志，输出当前设置的参数值
                logger.info("根据每个ParameterMapping中的TypeHandler设置对应的参数信息 value：{}", JSON.toJSONString(value));
                // 获取参数映射中的类型处理器
                TypeHandler typeHandler = parameterMapping.getTypeHandler();
                // 使用类型处理器将参数值设置到 PreparedStatement 对象中
                typeHandler.setParameter(ps, i + 1, value, jdbcType);
            }
        }
    }

}
