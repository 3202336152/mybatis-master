package com.huanyu.mybatis.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ClassName: InterceptorChain
 * Package: com.huanyu.mybatis.plugin
 * Description: 拦截器链
 *
 * @Author: 寰宇
 * @Create: 2024/6/27 20:26
 * @Version: 1.0
 */
public class InterceptorChain {
    // 拦截器链
    private final List<Interceptor> interceptors = new ArrayList<>();

    // target是支持拦截的几个类的实例。该方法依次向所有拦截器插入这几个类的实例
    // 如果某个插件真的需要发挥作用，则返回一个代理对象即可。如果不需要发挥作用，则返回原对象即可
    /**
     * 向所有的拦截器链提供目标对象，由拦截器链给出替换目标对象的对象
     * @param target 目标对象，是MyBatis中支持拦截的几个类（ParameterHandler、ResultSetHandler、StatementHandler、Executor）的实例
     * @return 用来替换目标对象的对象
     */
    public Object pluginAll(Object target) {
        // 依次交给每个拦截器完成目标对象的替换工作
        for (Interceptor interceptor : interceptors) {
            target = interceptor.plugin(target);
        }
        return target;
    }

    /**
     * 向拦截器链增加一个拦截器
     * @param interceptor 要增加的拦截器
     */
    public void addInterceptor(Interceptor interceptor) {
        interceptors.add(interceptor);
    }

    /**
     * 获取拦截器列表
     * @return 拦截器列表
     */
    public List<Interceptor> getInterceptors(){
        return Collections.unmodifiableList(interceptors);
    }

}
