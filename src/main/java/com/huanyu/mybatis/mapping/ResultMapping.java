package com.huanyu.mybatis.mapping;

import com.huanyu.mybatis.session.Configuration;
import com.huanyu.mybatis.type.TypeHandler;
import com.huanyu.mybatis.type.TypeHandlerRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: ResultMapping
 * Package: com.huanyu.mybatis.mapping
 * Description: 单个结果映射
 *
 * @Author: 寰宇
 * @Create: 2024/6/20 16:29
 * @Version: 1.0
 */
public class ResultMapping {

    private Configuration configuration;
    private String property;
    private String column;
    private Class<?> javaType;
    private TypeHandler<?> typeHandler;
    private List<ResultFlag> flags;

    ResultMapping() {
    }

    // 静态内部类，用于构建 ResultMapping 对象
    public static class Builder {

        // 内部 ResultMapping 对象，待构建
        private ResultMapping resultMapping = new ResultMapping();

        // 构造函数，初始化必要属性
        public Builder(Configuration configuration, String property, String column, Class<?> javaType) {
            resultMapping.configuration = configuration; // 设置配置对象
            resultMapping.property = property; // 设置属性名
            resultMapping.column = column; // 设置列名
            resultMapping.javaType = javaType; // 设置Java类型
            resultMapping.flags = new ArrayList<>(); // 初始化标志列表
        }

        // 设置类型处理器
        public Builder typeHandler(TypeHandler<?> typeHandler) {
            resultMapping.typeHandler = typeHandler; // 设置类型处理器
            return this; // 返回 Builder 对象自身以支持链式调用
        }

        // 设置标志列表
        public Builder flags(List<ResultFlag> flags) {
            resultMapping.flags = flags; // 设置标志列表
            return this; // 返回 Builder 对象自身以支持链式调用
        }

        // 构建并返回 ResultMapping 对象
        public ResultMapping build() {
            resolveTypeHandler(); // 解析并设置类型处理器（如果未设置）
            return resultMapping; // 返回构建好的 ResultMapping 对象
        }

        // 解析类型处理器
        private void resolveTypeHandler() {
            if (resultMapping.typeHandler == null && resultMapping.javaType != null) { // 如果类型处理器为空且Java类型不为空
                Configuration configuration = resultMapping.configuration; // 获取配置对象
                TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry(); // 获取类型处理器注册表
                resultMapping.typeHandler = typeHandlerRegistry.getTypeHandler(resultMapping.javaType, null); // 从注册表中获取类型处理器并设置
            }
        }
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public String getProperty() {
        return property;
    }

    public String getColumn() {
        return column;
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public TypeHandler<?> getTypeHandler() {
        return typeHandler;
    }

    public List<ResultFlag> getFlags() {
        return flags;
    }

}
