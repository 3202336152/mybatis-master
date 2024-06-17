package com.huanyu.mybatis.reflection.invoker;

/**
 * ClassName: Invoker
 * Package: com.huanyu.mybatis.reflection.invoker
 * Description: 调用者
 * Invoker 是一个接口，定义了反射调用的方法。它用于统一不同的调用方式（如方法调用、字段访问）。
 * @Author: 寰宇
 * @Create: 2024/6/17 14:49
 * @Version: 1.0
 */
public interface Invoker {

    // 方法执行调用器
    Object invoke(Object target, Object[] args) throws Exception;

    // 传入参数或者传出参数的类型（如有一个入参就是入参，否则是出参）
    Class<?> getType();
}
