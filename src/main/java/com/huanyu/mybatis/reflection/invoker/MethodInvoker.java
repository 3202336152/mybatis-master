package com.huanyu.mybatis.reflection.invoker;

import java.lang.reflect.Method;

/**
 * ClassName: MethodInvoker
 * Package: com.huanyu.mybatis.reflection.invoker
 * Description: 方法调用者
 * MethodInvoker 用于调用对象的方法。它封装了对方法的访问。
 * @Author: 寰宇
 * @Create: 2024/6/17 14:52
 * @Version: 1.0
 */
public class MethodInvoker implements Invoker {

    // 传入参数或者传出参数类型
    private Class<?> type;
    // 要操作的方法
    private Method method;

    /**
     * MethodInvoker构造方法
     * @param method 方法
     */
    public MethodInvoker(Method method) {
        this.method = method;

        // 如果只有一个参数，返回参数类型，否则返回 return 类型
        if (method.getParameterTypes().length == 1) {
            type = method.getParameterTypes()[0];
        } else {
            type = method.getReturnType();
        }
    }

    // 执行函数
    @Override
    public Object invoke(Object target, Object[] args) throws Exception {
        return method.invoke(target, args);
    }

    @Override
    public Class<?> getType() {
        return type;
    }
}
