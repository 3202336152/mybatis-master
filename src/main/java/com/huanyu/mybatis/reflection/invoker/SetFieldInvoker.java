package com.huanyu.mybatis.reflection.invoker;

import java.lang.reflect.Field;

/**
 * ClassName: SetFieldInvoker
 * Package: com.huanyu.mybatis.reflection.invoker
 * Description: setter 调用者
 * SetFieldInvoker 用于调用对象的字段的 set 方法。它封装了对字段的修改。
 * @Author: 寰宇
 * @Create: 2024/6/17 14:56
 * @Version: 1.0
 */
public class SetFieldInvoker implements Invoker {

    // 要操作的属性
    private Field field;

    public SetFieldInvoker(Field field) {
        this.field = field;
    }

    @Override
    public Object invoke(Object target, Object[] args) throws Exception {
        // 直接给属性赋值
        field.set(target, args[0]);
        return null;
    }

    @Override
    public Class<?> getType() {
        return field.getType();
    }
}
