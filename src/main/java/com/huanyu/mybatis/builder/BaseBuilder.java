package com.huanyu.mybatis.builder;

import com.huanyu.mybatis.session.Configuration;
import com.huanyu.mybatis.type.TypeAliasRegistry;

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

    public BaseBuilder(Configuration configuration) {
        this.configuration = configuration;
        this.typeAliasRegistry = this.configuration.getTypeAliasRegistry();
    }

    public Configuration getConfiguration() {
        return configuration;
    }

}
