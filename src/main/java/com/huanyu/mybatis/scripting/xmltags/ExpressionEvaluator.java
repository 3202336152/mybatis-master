package com.huanyu.mybatis.scripting.xmltags;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ClassName: ExpressionEvaluator
 * Package: com.huanyu.mybatis.scripting.xmltags
 * Description: 表达式求值器
 *
 * @Author: 寰宇
 * @Create: 2024/6/26 15:53
 * @Version: 1.0
 */
public class ExpressionEvaluator {

    /**
     * 对结果为true/false形式的表达式进行求值
     * 表达式求布尔值，比如 username == 'huanyu'
     * @param expression 表达式
     * @param parameterObject 参数对象
     * @return 求值结果
     */
    public boolean evaluateBoolean(String expression, Object parameterObject) {
        // 获取表达式的值，就是调用ognl
        Object value = OgnlCache.getValue(expression, parameterObject);
        // 如果确实是Boolean形式的结果
        if (value instanceof Boolean) {
            // 如果是Boolean
            return (Boolean) value;
        }
        // 如果是数值形式的结果
        if (value instanceof Number) {
            // 如果是Number，判断不为0
            return !new BigDecimal(String.valueOf(value)).equals(BigDecimal.ZERO);
        }
        // 否则判断不为null
        return value != null;
    }

    /**
     * 对结果为迭代形式的表达式进行求值
     * foreach 调用，暂时用不上。解析表达式到一个Iterable 核心是ognl
     * @param expression 表达式
     * @param parameterObject 参数对象
     * @return 求值结果
     */
    public Iterable<?> evaluateIterable(String expression, Object parameterObject) {
        // 原生的ognl很强大，OgnlCache.getValue 直接就可以返回一个Iterable型或数组型或Map型了
        Object value = OgnlCache.getValue(expression, parameterObject);
        if (value == null) {
            throw new RuntimeException("The expression '" + expression + "' evaluated to a null value.");
        }
        if (value instanceof Iterable) {
            return (Iterable<?>) value;
        }
        if (value.getClass().isArray()) {
            // 如果是array，则把他变成一个List<Object>
            // 注释下面提到了，不能用Arrays.asList()，因为array可能是基本型，这样会出ClassCastException，
            int size = Array.getLength(value);
            List<Object> answer = new ArrayList<Object>();
            for (int i = 0; i < size; i++) {
                Object o = Array.get(value, i);
                answer.add(o);
            }
            return answer;
        }
        if (value instanceof Map) {
            return ((Map) value).entrySet();
        }
        throw new RuntimeException("Error evaluating expression '" + expression + "'.  Return value (" + value + ") was not iterable.");
    }
}
