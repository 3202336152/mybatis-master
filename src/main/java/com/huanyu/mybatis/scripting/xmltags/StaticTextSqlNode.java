package com.huanyu.mybatis.scripting.xmltags;

/**
 * ClassName: StaticTextSqlNode
 * Package: com.huanyu.mybatis.scripting.defaults.xmltags
 * Description: 静态文本SQL节点，直接追加到sql尾部即可
 *
 * @Author: 寰宇
 * @Create: 2024/6/17 21:51
 * @Version: 1.0
 */
public class StaticTextSqlNode implements SqlNode {

    private String text;

    public StaticTextSqlNode(String text) {
        this.text = text;
    }

    // 最终调用到这里，将节点内容拼接到context后面
    @Override
    public boolean apply(DynamicContext context) {
        //将文本加入context
        context.appendSql(text);
        return true;
    }
}
