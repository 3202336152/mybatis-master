package com.huanyu.mybatis.reflection.wrapper;

import com.huanyu.mybatis.reflection.MetaObject;

/**
 * ClassName: SystemMetaObject
 * Package: com.huanyu.mybatis.reflection.wrapper
 * Description: 对象包装工厂
 *
 * @Author: 寰宇
 * @Create: 2024/6/17 15:11
 * @Version: 1.0
 */
public interface ObjectWrapperFactory {

    /**
     * 判断有没有包装器
     */
    boolean hasWrapperFor(Object object);

    /**
     * 得到包装器
     */
    ObjectWrapper getWrapperFor(MetaObject metaObject, Object object);

}
