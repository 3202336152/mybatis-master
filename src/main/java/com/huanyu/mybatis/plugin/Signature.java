package com.huanyu.mybatis.plugin;

/**
 * ClassName: Signature
 * Package: com.huanyu.mybatis.plugin
 * Description: 方法签名
 *
 * @Author: 寰宇
 * @Create: 2024/6/27 20:26
 * @Version: 1.0
 */
public @interface Signature {

    /**
     * 被拦截类
     */
    Class<?> type();

    /**
     * 被拦截类的方法
     */
    String method();

    /**
     * 被拦截类的方法的参数
     */
    Class<?>[] args();
}
