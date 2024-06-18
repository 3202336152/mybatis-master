package com.huanyu.mybatis.type;

import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * ClassName: TypeHandlerRegistry
 * Package: com.huanyu.mybatis.type
 * Description: 类型处理器注册机
 *
 * @Author: 寰宇
 * @Create: 2024/6/17 21:24
 * @Version: 1.0
 */
public final class TypeHandlerRegistry {
    // JDBC类型与对应类型处理器的映射
    private final Map<JdbcType, TypeHandler<?>> JDBC_TYPE_HANDLER_MAP = new EnumMap<>(JdbcType.class);
    // Java类型与Map<JdbcType, TypeHandler<?>>的映射
    private final Map<Type, Map<JdbcType, TypeHandler<?>>> TYPE_HANDLER_MAP = new HashMap<>();
    // 键为typeHandler.getClass() ，值为typeHandler。里面存储了所有的类型处理器
    private final Map<Class<?>, TypeHandler<?>> ALL_TYPE_HANDLERS_MAP = new HashMap<>();

    public TypeHandlerRegistry() {

    }

    private void register(Type javaType, JdbcType jdbcType, TypeHandler<?> handler) {
        if (null != javaType) {
            Map<JdbcType, TypeHandler<?>> map = TYPE_HANDLER_MAP.computeIfAbsent(javaType, k -> new HashMap<>());
            map.put(jdbcType, handler);
        }
        ALL_TYPE_HANDLERS_MAP.put(handler.getClass(), handler);
    }
}
