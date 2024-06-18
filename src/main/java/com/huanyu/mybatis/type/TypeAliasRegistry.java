package com.huanyu.mybatis.type;

import com.huanyu.mybatis.io.Resources;

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
        try {
            // 检查传入的字符串是否为 null，如果是 null 则返回 null
            if (string == null) {
                return null;
            }
            // 将传入的字符串转换为小写，便于在 TYPE_ALIASES 中查找
            String key = string.toLowerCase(Locale.ENGLISH);
            // 定义一个 Class<T> 类型的变量，用于存储找到的类
            Class<T> value;
            // 检查 TYPE_ALIASES 是否包含该 key
            if (TYPE_ALIASES.containsKey(key)) {
                // 如果包含，则从 TYPE_ALIASES 中获取对应的类
                value = (Class<T>) TYPE_ALIASES.get(key);
            } else {
                // 如果不包含，则尝试通过类名加载该类
                value = (Class<T>) Resources.classForName(string);
            }
            // 返回找到的类
            return value;
        } catch (ClassNotFoundException e) {
            // 如果类未找到，捕获 ClassNotFoundException 并抛出一个带有详细信息的运行时异常
            throw new RuntimeException("Could not resolve type alias '" + string + "'.  Cause: " + e, e);
        }
    }

}
