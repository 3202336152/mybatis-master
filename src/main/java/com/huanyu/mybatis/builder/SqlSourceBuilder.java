package com.huanyu.mybatis.builder;

import com.huanyu.mybatis.mapping.ParameterMapping;
import com.huanyu.mybatis.mapping.SqlSource;
import com.huanyu.mybatis.prasing.GenericTokenParser;
import com.huanyu.mybatis.prasing.TokenHandler;
import com.huanyu.mybatis.reflection.MetaClass;
import com.huanyu.mybatis.reflection.MetaObject;
import com.huanyu.mybatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ClassName: SqlSourceBuilder
 * Package: com.huanyu.mybatis.builder
 * Description: SQL 源码构建器
 * SqlSource的解析器
 * @Author: 寰宇
 * @Create: 2024/6/17 21:29
 * @Version: 1.0
 */
public class SqlSourceBuilder extends BaseBuilder {

    private static Logger logger = LoggerFactory.getLogger(SqlSourceBuilder.class);

    // 能够处理的占位符属性
    private static final String parameterProperties = "javaType,jdbcType,mode,numericScale,resultMap,typeHandler,jdbcTypeName";

    public SqlSourceBuilder(Configuration configuration) {
        super(configuration);
    }

    // 这里解析的对象是SqlNode拼接结束的，即<if> <where>等节点的结果都已经解析结束。然后在这里继续处理
    /**
     * 将DynamicSqlSource和RawSqlSource中的“#{}”符号替换掉，从而将他们转化为StaticSqlSource
     * @param originalSql sqlNode.apply()拼接之后的sql语句。已经不包含<if> <where>等节点，也不含有${}符号
     * @param parameterType 实参类型
     * @param additionalParameters 附加参数
     * @return 解析结束的StaticSqlSource
     */
    public SqlSource parse(String originalSql, Class<?> parameterType, Map<String, Object> additionalParameters) {
        // 用来完成#{}处理的处理器
        ParameterMappingTokenHandler handler = new ParameterMappingTokenHandler(configuration, parameterType, additionalParameters);
        // 通用的占位符解析器，用来进行占位符替换
        GenericTokenParser parser = new GenericTokenParser("#{", "}", handler);
        // 将#{}替换为?的SQL语句
        String sql = parser.parse(originalSql);
        // 返回静态 SQL
        return new StaticSqlSource(configuration, sql, handler.getParameterMappings());
    }

    // 用以替换占位符的处理器
    // 用来处理形如#｛ id, javaType= int, jdbcType=NUMERIC, typeHandler=DemoTypeHandler ｝
    private static class ParameterMappingTokenHandler extends BaseBuilder implements TokenHandler {

        // 每个#{}中的东西对应一个ParameterMapping。所有的#{}都放在这个list
        private List<ParameterMapping> parameterMappings = new ArrayList<>();
        // 参数类型
        private Class<?> parameterType;
        // 参数的Meta对象
        private MetaObject metaParameters;

        public ParameterMappingTokenHandler(Configuration configuration, Class<?> parameterType, Map<String, Object> additionalParameters) {
            super(configuration);
            this.parameterType = parameterType;
            this.metaParameters = configuration.newMetaObject(additionalParameters);
        }

        public List<ParameterMapping> getParameterMappings() {
            return parameterMappings;
        }

        /**
         * 在这里，${}被替换为？
         * 但同时，用户传入的实际参数也被记录了
         * @param content 包含
         * @return
         */
        @Override
        public String handleToken(String content) {
            parameterMappings.add(buildParameterMapping(content));
            return "?";
        }

        // 构建参数映射
        private ParameterMapping buildParameterMapping(String content) {
            // 解析参数映射，将参数表达式转换为一个 HashMap，例如：#{favouriteSection,jdbcType=VARCHAR}
            Map<String, String> propertiesMap = new ParameterExpression(content);

            // 获取参数映射中的属性名
            String property = propertiesMap.get("property");
            Class<?> propertyType;

            // 判断 typeHandlerRegistry 是否有该参数类型的处理器
            if (typeHandlerRegistry.hasTypeHandler(parameterType)) {
                // 如果有处理器，直接使用参数类型
                propertyType = parameterType;
            } else if (property != null) {
                // 如果属性名不为空
                MetaClass metaClass = MetaClass.forClass(parameterType);
                // 检查 MetaClass 是否有该属性的 getter 方法
                if (metaClass.hasGetter(property)) {
                    // 如果有 getter 方法，获取属性类型
                    propertyType = metaClass.getGetterType(property);
                } else {
                    // 如果没有 getter 方法，默认使用 Object 类
                    propertyType = Object.class;
                }
            } else {
                // 如果属性名为空，默认使用 Object 类
                propertyType = Object.class;
            }

            // 记录日志，输出当前构建的参数映射的属性名和属性类型
            logger.info("构建参数映射 property：{} propertyType：{}", property, propertyType);

            // 使用 ParameterMapping.Builder 构建 ParameterMapping 对象
            ParameterMapping.Builder builder = new ParameterMapping.Builder(configuration, property, propertyType);
            return builder.build();
        }

    }
}
