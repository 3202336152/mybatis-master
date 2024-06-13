package com.huanyu.mybatis.type;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * ClassName: TypeAliasRegistry
 * Package: com.huanyu.mybatis.type
 * Description: 类型别名注册机
 *
 * @Author: 寰宇
 * @Create: 2024/6/13 16:13
 * @Version: 1.0
 */
public class TypeAliasRegistry {
    private final Map<String, Class<?>> TYPE_ALIASES = new HashMap<>();

    public TypeAliasRegistry() {
        // 构造函数里注册系统内置的类型别名
        registerAlias("string", String.class);

        // 基本包装类型
        registerAlias("byte", Byte.class);
        registerAlias("long", Long.class);
        registerAlias("short", Short.class);
        registerAlias("int", Integer.class);
        registerAlias("integer", Integer.class);
        registerAlias("double", Double.class);
        registerAlias("float", Float.class);
        registerAlias("boolean", Boolean.class);
    }

    public void registerAlias(String alias, Class<?> value) {
        String key = alias.toLowerCase(Locale.ENGLISH);
        TYPE_ALIASES.put(key, value);
    }

    public <T> Class<T> resolveAlias(String string) {
        String key = string.toLowerCase(Locale.ENGLISH);
        return (Class<T>) TYPE_ALIASES.get(key);
    }
}
