package com.huanyu.mybatis.scripting.xmltags;

import java.util.List;

/**
 * ClassName: MixedSqlNode
 * Package: com.huanyu.mybatis.scripting.defaults.xmltags
 * Description: 混合SQL节点，需要将节点内的内容循环加到尾部
 *
 * @Author: 寰宇
 * @Create: 2024/6/17 21:50
 * @Version: 1.0
 */
public class MixedSqlNode implements SqlNode{
    //组合模式，拥有一个SqlNode的List
    private List<SqlNode> contents;

    public MixedSqlNode(List<SqlNode> contents) {
        this.contents = contents;
    }

    @Override
    public boolean apply(DynamicContext context) {
        // 依次调用list里每个元素的apply
        contents.forEach(node -> node.apply(context));
        return true;
    }
}
