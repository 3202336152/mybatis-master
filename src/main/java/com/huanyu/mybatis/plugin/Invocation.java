package com.huanyu.mybatis.plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * ClassName: Invocation
 * Package: com.huanyu.mybatis.plugin
 * Description: 调用信息
 * 代表了一个调用的详细信息
 * @Author: 寰宇
 * @Create: 2024/6/27 20:25
 * @Version: 1.0
 */
public class Invocation {

    // 调用的对象
    private Object target;
    // 调用的方法
    private Method method;
    // 调用的参数
    private Object[] args;

    public Invocation(Object target, Method method, Object[] args) {
        this.target = target;
        this.method = method;
        this.args = args;
    }

    public Object getTarget() {
        return target;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getArgs() {
        return args;
    }

    // 放行；调用执行
    public Object proceed() throws InvocationTargetException, IllegalAccessException {
        return method.invoke(target, args);
    }
}
