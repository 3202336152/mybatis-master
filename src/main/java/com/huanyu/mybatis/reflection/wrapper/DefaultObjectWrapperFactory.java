package com.huanyu.mybatis.reflection.wrapper;

import com.huanyu.mybatis.reflection.MetaObject;

/**
 * ClassName: SystemMetaObject
 * Package: com.huanyu.mybatis.reflection.wrapper
 * Description: 默认对象包装工厂
 * DefaultObjectWrapperFactory 是默认的对象包装器工厂，用于创建合适的 ObjectWrapper 实例。
 * @Author: 寰宇
 * @Create: 2024/6/17 15:11
 * @Version: 1.0
 */
public class DefaultObjectWrapperFactory implements ObjectWrapperFactory{

    @Override
    public boolean hasWrapperFor(Object object) {
        return false;
    }

    @Override
    public ObjectWrapper getWrapperFor(MetaObject metaObject, Object object) {
        throw new RuntimeException("The DefaultObjectWrapperFactory should never be called to provide an ObjectWrapper.");
    }

}
