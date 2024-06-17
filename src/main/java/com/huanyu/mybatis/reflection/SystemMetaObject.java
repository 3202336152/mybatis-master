package com.huanyu.mybatis.reflection;

import com.huanyu.mybatis.reflection.factory.DefaultObjectFactory;
import com.huanyu.mybatis.reflection.factory.ObjectFactory;
import com.huanyu.mybatis.reflection.wrapper.DefaultObjectWrapperFactory;
import com.huanyu.mybatis.reflection.wrapper.ObjectWrapperFactory;

/**
 * ClassName: SystemMetaObject
 * Package: com.huanyu.mybatis.reflection
 * Description: 系统级别的元对象
 *
 * @Author: 寰宇
 * @Create: 2024/6/17 15:11
 * @Version: 1.0
 */
public class SystemMetaObject {

    public static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();
    public static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();
    public static final MetaObject NULL_META_OBJECT = MetaObject.forObject(NullObject.class, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY);

    private SystemMetaObject() {
        // Prevent Instantiation of Static Class
    }

    /**
     * 空对象
     */
    private static class NullObject {
    }

    public static MetaObject forObject(Object object) {
        return MetaObject.forObject(object, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY);
    }

}
