package com.huanyu.mybatis.reflection.wrapper;

import com.huanyu.mybatis.reflection.MetaObject;
import com.huanyu.mybatis.reflection.factory.ObjectFactory;
import com.huanyu.mybatis.reflection.property.PropertyTokenizer;

import java.util.List;

/**
 * ClassName: SystemMetaObject
 * Package: com.huanyu.mybatis.reflection.wrapper
 * Description: 对象包装器
 * ObjectWrapper 是一个接口，定义了包装对象的方法。它用于统一处理不同类型的对象（如 Bean、集合、Map）。
 * @Author: 寰宇
 * @Create: 2024/6/17 15:11
 * @Version: 1.0
 */
public interface ObjectWrapper {

    // get
    Object get(PropertyTokenizer prop);

    // set
    void set(PropertyTokenizer prop, Object value);

    // 查找属性
    String findProperty(String name, boolean useCamelCaseMapping);

    // 取得getter的名字列表
    String[] getGetterNames();

    // 取得setter的名字列表
    String[] getSetterNames();

    //取得setter的类型
    Class<?> getSetterType(String name);

    // 取得getter的类型
    Class<?> getGetterType(String name);

    // 是否有指定的setter
    boolean hasSetter(String name);

    // 是否有指定的getter
    boolean hasGetter(String name);

    // 实例化属性
    MetaObject instantiatePropertyValue(String name, PropertyTokenizer prop, ObjectFactory objectFactory);

    // 是否是集合
    boolean isCollection();

    // 添加属性
    void add(Object element);

    // 添加属性
    <E> void addAll(List<E> element);

}
