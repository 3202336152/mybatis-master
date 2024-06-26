package com.huanyu.mybatis.builder;

import com.huanyu.mybatis.session.Configuration;
import com.huanyu.mybatis.type.TypeAliasRegistry;
import com.huanyu.mybatis.type.TypeHandler;
import com.huanyu.mybatis.type.TypeHandlerRegistry;

/**
 * ClassName: BaseBuilder
 * Package: com.huanyu.mybatis.builder
 * Description: 构建器的基类，建造者模式
 *
 * @Author: 寰宇
 * @Create: 2024/6/12 14:47
 * @Version: 1.0
 */
public abstract class BaseBuilder {

    protected final Configuration configuration;
    protected final TypeAliasRegistry typeAliasRegistry;

    protected final TypeHandlerRegistry typeHandlerRegistry;


    public BaseBuilder(Configuration configuration) {
        this.configuration = configuration;
        this.typeAliasRegistry = this.configuration.getTypeAliasRegistry();
        typeHandlerRegistry = this.configuration.getTypeHandlerRegistry();
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    // 根据别名创建handler
    protected Class<?> resolveAlias(String alias) {
        return typeAliasRegistry.resolveAlias(alias);
    }

    // 根据别名解析 Class 类型别名注册/事务管理器别名
    protected Class<?> resolveClass(String alias) {
        if (alias == null) {
            return null;
        }
        try {
            return resolveAlias(alias);
        } catch (Exception e) {
            throw new RuntimeException("Error resolving class. Cause: " + e, e);
        }
    }

    // 根据javaType和handlerType获取handler
    protected TypeHandler<?> resolveTypeHandler(Class<?> javaType, Class<? extends TypeHandler<?>> typeHandlerType) {
        if (typeHandlerType == null){
            return null;
        }
        return typeHandlerRegistry.getMappingTypeHandler(typeHandlerType);
    }

}
