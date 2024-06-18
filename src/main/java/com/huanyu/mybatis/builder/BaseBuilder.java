package com.huanyu.mybatis.builder;

import com.huanyu.mybatis.session.Configuration;
import com.huanyu.mybatis.type.TypeAliasRegistry;
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

}
