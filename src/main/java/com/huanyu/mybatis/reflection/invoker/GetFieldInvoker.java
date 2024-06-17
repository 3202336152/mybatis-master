package com.huanyu.mybatis.reflection.invoker;

import java.lang.reflect.Field;

/**
 * ClassName: GetFieldInvoker
 * Package: com.huanyu.mybatis.reflection.invoker
 * Description: getter 调用者
 * GetFieldInvoker 用于调用对象的字段的 get 方法。它封装了对字段的访问。
 * @Author: 寰宇
 * @Create: 2024/6/17 14:54
 * @Version: 1.0
 */
public class GetFieldInvoker implements Invoker {

    // 要操作的属性
    private Field field;

    public GetFieldInvoker(Field field) {
        this.field = field;
    }

    /**
     * 执行方法
     * @param target 目标对象
     * @param args 方法入参
     * @return 方法的返回结果
     * @throws IllegalAccessException
     */
    @Override
    public Object invoke(Object target, Object[] args) throws Exception {
        return field.get(target);
    }

    @Override
    public Class<?> getType() {
        return field.getType();
    }
}
