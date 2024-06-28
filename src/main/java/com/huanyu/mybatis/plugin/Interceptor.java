package com.huanyu.mybatis.plugin;

import java.util.Properties;

/**
 * ClassName: Interceptor
 * Package: com.huanyu.mybatis.plugin
 * Description: 拦截器接口
 * 是插件接口，所有插件需要实现该接口
 * @Author: 寰宇
 * @Create: 2024/6/27 20:25
 * @Version: 1.0
 */
public interface Interceptor {

    /**
     * 该方法内是拦截器拦截到目标方法时的操作
     * @param invocation 拦截到的目标方法的信息
     * @return 经过拦截器处理后的返回结果
     * @throws Throwable
     */
    Object intercept(Invocation invocation) throws Throwable;

    /**
     * 用返回值替代入参对象。
     * 通常情况下，可以调用Plugin的warp方法来完成，因为warp方法能判断目标对象是否需要拦截，并根据判断结果返回相应的对象来替换目标对象
     * @param target MyBatis传入的支持拦截的几个类（ParameterHandler、ResultSetHandler、StatementHandler、Executor）的实例
     * @return 如果当前拦截器要拦截该实例，则返回该实例的代理；如果不需要拦截该实例，则直接返回该实例本身
     */
    default Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    /**
     * 设置拦截器的属性
     * @param properties 要给拦截器设置的属性
     */
    default void setProperties(Properties properties) {
        // NOP
    }
}
