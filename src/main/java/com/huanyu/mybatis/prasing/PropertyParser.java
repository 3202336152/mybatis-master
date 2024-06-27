package com.huanyu.mybatis.prasing;

import java.util.Properties;

/**
 * ClassName: PropertyParser
 * Package: com.huanyu.mybatis.prasing
 * Description:
 *
 * @Author: 寰宇
 * @Create: 2024/6/27 13:14
 * @Version: 1.0
 */
public class PropertyParser {

    private static final String KEY_PREFIX = "org.apache.ibatis.parsing.PropertyParser.";
    /**
     * The special property key that indicate whether enable a default value on placeholder.
     * <p>
     *   The default value is {@code false} (indicate disable a default value on placeholder)
     *   If you specify the {@code true}, you can specify key and default value on placeholder (e.g. {@code ${db.username:postgres}}).
     * </p>
     * @since 3.4.2
     */
    public static final String KEY_ENABLE_DEFAULT_VALUE = KEY_PREFIX + "enable-default-value";

    /**
     * The special property key that specify a separator for key and default value on placeholder.
     * <p>
     *   The default separator is {@code ":"}.
     * </p>
     * @since 3.4.2
     */
    public static final String KEY_DEFAULT_VALUE_SEPARATOR = KEY_PREFIX + "default-value-separator";

    private static final String ENABLE_DEFAULT_VALUE = "false";
    private static final String DEFAULT_VALUE_SEPARATOR = ":";

    private PropertyParser() {
        // Prevent Instantiation
    }

    /**
     * 进行字符串中属性变量的替换
     * @param string 输入的字符串，可能包含属性变量
     * @param variables 属性映射信息
     * @return 经过属性变量替换的字符串
     */
    public static String parse(String string, Properties variables) {
        // 创建负责字符串替换的类
        VariableTokenHandler handler = new VariableTokenHandler(variables);
        // 创建通用的占位符解析器
        GenericTokenParser parser = new GenericTokenParser("${", "}", handler);
        // 开展解析，即替换占位符中的值
        return parser.parse(string);
    }

    private static class VariableTokenHandler implements TokenHandler {
        // 输入的属性变量，是HashTable的子类
        private final Properties variables;
        // 是否启用默认值
        private final boolean enableDefaultValue;
        // 如果启用默认值的化，键和默认值之间的分割符
        private final String defaultValueSeparator;

        private VariableTokenHandler(Properties variables) {
            this.variables = variables;
            // 如果传入的属性中有就以属性中为准，否则设为默认值
            // 默认不支持默认值，除非在属性中用org.apache.ibatis.parsing.PropertyParser.enable-default-value修改
            this.enableDefaultValue = Boolean.parseBoolean(getPropertyValue(KEY_ENABLE_DEFAULT_VALUE, ENABLE_DEFAULT_VALUE));
            // 默认分隔符为“：”，除非在属性中用org.apache.ibatis.parsing.PropertyParser.default-value-separator修改
            this.defaultValueSeparator = getPropertyValue(KEY_DEFAULT_VALUE_SEPARATOR, DEFAULT_VALUE_SEPARATOR);
        }

        // 取出variables中指定key的value，如果variables为null,则给出defaultValue
        private String getPropertyValue(String key, String defaultValue) {
            return (variables == null) ? defaultValue : variables.getProperty(key, defaultValue);
        }


        /**
         * 根据一个字符串，给出另一个字符串。多用在字符串替换等处
         * 具体实现中，会以content作为键,从variables找出并返回对应的值
         * 由键寻值的过程中支持设置默认值
         * 如果启用默认值，则content形如"key:defaultValue"
         * 如果没有启用默认值，则content形如"key"
         * @param content 输入的字符串
         * @return 输出的字符串
         */
        @Override
        public String handleToken(String content) {
            if (variables != null) { // variables不为null
                String key = content;
                if (enableDefaultValue) { // 如果启用默认值，则设置默认值
                    // 找出键与默认值分割符的位置
                    final int separatorIndex = content.indexOf(defaultValueSeparator);
                    String defaultValue = null;
                    if (separatorIndex >= 0) {
                        // 分隔符以前是键
                        key = content.substring(0, separatorIndex);
                        // 分隔符以后是默认值
                        defaultValue = content.substring(separatorIndex + defaultValueSeparator.length());
                    }
                    if (defaultValue != null) {
                        return variables.getProperty(key, defaultValue);
                    }
                }
                if (variables.containsKey(key)) {
                    // 尝试寻找非默认的值
                    return variables.getProperty(key);
                }
            }
            // 如果variables为null。不发生任何替换，直接原样还回
            return "${" + content + "}";
        }
    }
}
