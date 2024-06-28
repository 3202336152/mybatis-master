package com.huanyu.mybatis.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ClassName: Intercepts
 * Package: com.huanyu.mybatis.plugin
 * Description: 拦截注解
 *
 * @Author: 寰宇
 * @Create: 2024/6/27 20:25
 * @Version: 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Intercepts {

    Signature[] value();

}
