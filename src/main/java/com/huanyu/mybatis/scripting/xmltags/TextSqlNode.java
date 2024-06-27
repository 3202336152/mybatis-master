package com.huanyu.mybatis.scripting.xmltags;

import com.huanyu.mybatis.prasing.GenericTokenParser;
import com.huanyu.mybatis.prasing.TokenHandler;
import com.huanyu.mybatis.type.SimpleTypeRegistry;

import java.util.regex.Pattern;

/**
 * ClassName: TextSqlNode
 * Package: com.huanyu.mybatis.scripting.xmltags
 * Description: 文本SQL节点(CDATA|TEXT)
 * 包含${}占位符的动态sql节点
 * @Author: 寰宇
 * @Create: 2024/6/26 16:04
 * @Version: 1.0
 */
public class TextSqlNode implements SqlNode {
    private String text;
    private Pattern injectionFilter;

    public TextSqlNode(String text) {
        this(text, null);
    }

    public TextSqlNode(String text, Pattern injectionFilter) {
        this.text = text;
        this.injectionFilter = injectionFilter;
    }

    /**
     * 判断当前节点是不是动态的
     * @return 节点是否为动态
     */
    public boolean isDynamic() {
        // 占位符处理器，该处理器并不会处理占位符，而是判断是不是含有占位符
        DynamicCheckerTokenParser checker = new DynamicCheckerTokenParser();
        GenericTokenParser parser = createParser(checker);
        // 使用占位符处理器。如果节点内容中含有占位符，则DynamicCheckerTokenParser对象的isDynamic属性将会被置为true
        parser.parse(text);
        return checker.isDynamic();
    }

    /**
     * 完成该节点自身的解析
     * @param context 上下文环境，节点自身的解析结果将合并到该上下文环境中
     * @return 解析是否成功
     */
    @Override
    public boolean apply(DynamicContext context) {
        // 创建通用的占位符解析器
        GenericTokenParser parser = createParser(new BindingTokenParser(context, injectionFilter));
        // 替换掉其中的${}占位符
        context.appendSql(parser.parse(text));
        return true;
    }

    /**
     * 创建一个通用的占位符解析器，用来解析${}占位符
     * @param handler 用来处理${}占位符的专用处理器
     * @return 占位符解析器
     */
    private GenericTokenParser createParser(TokenHandler handler) {
        return new GenericTokenParser("${", "}", handler);
    }

    // 绑定记号解析器
    private static class BindingTokenParser implements TokenHandler {

        private DynamicContext context;
        private Pattern injectionFilter;

        public BindingTokenParser(DynamicContext context, Pattern injectionFilter) {
            this.context = context;
            this.injectionFilter = injectionFilter;
        }

        @Override
        public String handleToken(String content) {
            // 拿到用户给出的实参
            Object parameter = context.getBindings().get("_parameter");
            if (parameter == null) {
                context.getBindings().put("value", null);
            } else if (SimpleTypeRegistry.isSimpleType(parameter.getClass())) {
                context.getBindings().put("value", parameter);
            }
            // 根据上下文信息，得到占位符中内容的值
            Object value = OgnlCache.getValue(content, context.getBindings());
            String srtValue = (value == null ? "" : String.valueOf(value)); // issue #274 return "" instead of "null"
            checkInjection(srtValue);
            return srtValue;
        }

        // 检查是否匹配正则表达式
        private void checkInjection(String value) {
            if (injectionFilter != null && !injectionFilter.matcher(value).matches()) {
                throw new RuntimeException("Invalid input. Please conform to regex" + injectionFilter.pattern());
            }
        }
    }

    /**
     * 动态SQL检查器
     */
    private static class DynamicCheckerTokenParser implements TokenHandler {

        private boolean isDynamic;

        public DynamicCheckerTokenParser() {
            // Prevent Synthetic Access
        }

        public boolean isDynamic() {
            return isDynamic;
        }

        @Override
        public String handleToken(String content) {
            // 设置 isDynamic 为 true，即调用了这个类就必定是动态 SQL
            this.isDynamic = true;
            return null;
        }
    }

}
