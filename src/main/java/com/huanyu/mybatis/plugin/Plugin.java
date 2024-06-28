package com.huanyu.mybatis.plugin;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * ClassName: Plugin
 * Package: com.huanyu.mybatis.plugin
 * Description: 代理模式插件
 *
 * @Author: 寰宇
 * @Create: 2024/6/27 20:26
 * @Version: 1.0
 */
public class Plugin implements InvocationHandler {

    // 被代理对象
    private Object target;
    // 拦截器
    private Interceptor interceptor;
    // 拦截器要拦截的所有的类，以及类中的方法
    private Map<Class<?>, Set<Method>> signatureMap;

    private Plugin(Object target, Interceptor interceptor, Map<Class<?>, Set<Method>> signatureMap) {
        this.target = target;
        this.interceptor = interceptor;
        this.signatureMap = signatureMap;
    }

    /**
     * 代理对象的拦截方法，当被代理对象中方法被触发时会进入这里
     * @param proxy 代理类
     * @param method 被触发的方法
     * @param args 被触发的方法的参数
     * @return 被触发的方法的返回结果
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 获取该类所有需要拦截的方法
        Set<Method> methods = signatureMap.get(method.getDeclaringClass());
        // 过滤需要拦截的方法
        if (methods != null && methods.contains(method)) {
            // 调用 Interceptor#intercept 插入自己的反射逻辑
            // 方法确实需要被拦截器拦截，因此交给拦截器处理
            return interceptor.intercept(new Invocation(target, method, args));
        }
        // 这说明该方法不需要拦截，交给被代理对象处理
        return method.invoke(target, args);
    }

    /**
     * 根据拦截器的配置来生成一个对象用来替换被代理对象
     * @param target 被代理对象
     * @param interceptor 拦截器
     * @return 用来替换被代理对象的对象
     */
    public static Object wrap(Object target, Interceptor interceptor) {
        // 得到拦截器interceptor要拦截的类型与方法
        Map<Class<?>, Set<Method>> signatureMap = getSignatureMap(interceptor);
        // 取得要改变行为的类(ParameterHandler|ResultSetHandler|StatementHandler|Executor)，目前只添加了 StatementHandler
        // 被代理对象的类型
        Class<?> type = target.getClass();
        // 取得接口
        // 逐级寻找被代理对象类型的父类，将父类中需要被拦截的全部找出
        Class<?>[] interfaces = getAllInterfaces(type, signatureMap);
        // 创建代理(StatementHandler)
        // 只要父类中有一个需要拦截，说明被代理对象是需要拦截的
        if (interfaces.length > 0) {
            // Proxy.newProxyInstance(ClassLoader loader, Class<?>[] interfaces, InvocationHandler h)
            return Proxy.newProxyInstance(
                    type.getClassLoader(),
                    interfaces,
                    new Plugin(target, interceptor, signatureMap));
        }
        // 直接返回原有被代理对象，这意味着被代理对象的方法不需要被拦截
        return target;
    }

    /**
     * 获取拦截器要拦截的所有类和类中的方法
     * 得到该拦截器interceptor要拦截的类型与方法。Map<Class<?>, Set<Method>> 中键为类型，值为该类型内的方法集合
     * @param interceptor 拦截器
     * @return 入参拦截器要拦截的所有类和类中的方法
     */
    private static Map<Class<?>, Set<Method>> getSignatureMap(Interceptor interceptor) {
        // 取 Intercepts 注解，例子可参见 QueryPlugin.java
        Intercepts interceptsAnnotation = interceptor.getClass().getAnnotation(Intercepts.class);
        // 必须得有 Intercepts 注解，没有报错
        if (interceptsAnnotation == null) {
            throw new RuntimeException("No @Intercepts annotation was found in interceptor " + interceptor.getClass().getName());
        }
        // 将Intercepts注解的value信息取出来，是一个Signature数组
        Signature[] sigs = interceptsAnnotation.value();
        // 将Signature数组数组放入一个Map中。键为Signature注解的type类型，值为该类型下的方法集合
        Map<Class<?>, Set<Method>> signatureMap = new HashMap<>();
        for (Signature sig : sigs) {
            Set<Method> methods = signatureMap.computeIfAbsent(sig.type(), k -> new HashSet<>());
            try {
                // 获取到方法：StatementHandler.prepare(Connection connection)、StatementHandler.parameterize(Statement statement)...
                Method method = sig.type().getMethod(sig.method(), sig.args());
                methods.add(method);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Could not find method on " + sig.type() + " named " + sig.method() + ". Cause: " + e, e);
            }
        }
        return signatureMap;
    }

    /**
     * 逐级寻找目标类的父类，判断是否有父类需要被拦截器拦截
     * @param type 目标类类型
     * @param signatureMap 拦截器要拦截的所有类和类中的方法
     * @return 拦截器要拦截的所有父类的列表
     */
    private static Class<?>[] getAllInterfaces(Class<?> type, Map<Class<?>, Set<Method>> signatureMap) {
        Set<Class<?>> interfaces = new HashSet<Class<?>>();
        while (type != null) {
            for (Class<?> c : type.getInterfaces()) {
                // 拦截 ParameterHandler|ResultSetHandler|StatementHandler|Executor
                if (signatureMap.containsKey(c)) {
                    interfaces.add(c);
                }
            }
            type = type.getSuperclass();
        }
        return interfaces.toArray(new Class<?>[interfaces.size()]);
    }
}
