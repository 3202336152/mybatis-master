package com.huanyu.mybatis.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ClassName: Delete
 * Package: com.huanyu.mybatis.annotations
 * Description: delete 语句注解
 *
 * @Author: 寰宇
 * @Create: 2024/6/23 15:27
 * @Version: 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Delete {

    String[] value();
}
