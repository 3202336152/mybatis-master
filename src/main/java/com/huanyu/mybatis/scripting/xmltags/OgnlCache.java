package com.huanyu.mybatis.scripting.xmltags;

import ognl.Ognl;
import ognl.OgnlException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ClassName: OgnlCache
 * Package: com.huanyu.mybatis.scripting.xmltags
 * Description: OGNL缓存
 * OGNL 是 Object-Graph Navigation Language 的缩写，它是一种功能强大的表达式语言（Expression Language，简称为EL）
 * 通过它简单一致的表达式语法，可以存取对象的任意属性，调用对象的方法，遍历整个对象的结构图，实现字段类型转化等功能。
 * 它使用相同的表达式去存取对象的属性。
 * @Author: 寰宇
 * @Create: 2024/6/26 15:55
 * @Version: 1.0
 */
public class OgnlCache {

    // 缓存解析后的OGNL表达式,用以提高效率
    private static final Map<String, Object> expressionCache = new ConcurrentHashMap<String, Object>();

    private OgnlCache() {
        // Prevent Instantiation of Static Class
    }

    /**
     * 读取表达式的结果
     * @param expression 表达式
     * @param root 根环境
     * @return 表达式结果
     */
    public static Object getValue(String expression, Object root) {
        try {
            // 创建默认的上下文环境
            Map<Object, OgnlClassResolver> context = Ognl.createDefaultContext(root, new OgnlClassResolver());
            // 依次传入表达式树、上下文、根，从而获得表达式的结果
            return Ognl.getValue(parseExpression(expression), context, root);
        } catch (OgnlException e) {
            throw new RuntimeException("Error evaluating expression '" + expression + "'. Cause: " + e, e);
        }
    }

    /**
     * 解析表达式，得到解析后的表达式树
     * @param expression 表达式
     * @return 表达式树
     * @throws OgnlException
     */
    private static Object parseExpression(String expression) throws OgnlException {
        // 先从缓存中获取
        Object node = expressionCache.get(expression);
        if (node == null) {
            // 缓存没有则直接解析，并放入缓存
            node = Ognl.parseExpression(expression);
            expressionCache.put(expression, node);
        }
        return node;
    }
}
