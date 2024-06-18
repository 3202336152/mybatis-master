package com.huanyu.mybatis.scripting.xmltags;

import com.huanyu.mybatis.builder.BaseBuilder;
import com.huanyu.mybatis.mapping.SqlSource;
import com.huanyu.mybatis.scripting.defaults.RawSqlSource;
import com.huanyu.mybatis.session.Configuration;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: XMLScriptBuilder
 * Package: com.huanyu.mybatis.scripting.defaults.xmltags
 * Description: XML脚本构建器
 *
 * @Author: 寰宇
 * @Create: 2024/6/17 21:52
 * @Version: 1.0
 */
public class XMLScriptBuilder extends BaseBuilder {

    // 当前要处理的XML节点
    private Element element;
    // 当前节点是否为动态节点
    private boolean isDynamic;
    // 输入参数的类型
    private Class<?> parameterType;

    public XMLScriptBuilder(Configuration configuration, Element element, Class<?> parameterType) {
        super(configuration);
        this.element = element;
        this.parameterType = parameterType;
    }

    /**
     * 解析节点生成SqlSource对象
     * @return SqlSource对象
     */
    public SqlSource parseScriptNode() {
        // 解析XML节点节点，得到节点树MixedSqlNode
        List<SqlNode> contents = parseDynamicTags(element);
        MixedSqlNode rootSqlNode = new MixedSqlNode(contents);
        return new RawSqlSource(configuration, rootSqlNode, parameterType);
    }

    /**
     * 解析动态标签节点 <if> <where> <set> <foreach>
     *           动态标签节点的解析，暂时只支持 <if> <where> <set> <foreach>
     *               1. 解析标签内容，得到动态SQL片段
     *               2. 解析动态SQL片段，得到SqlNode对象
     * @param element 动态标签节点
     * @return 动态SQL片段对应的SqlNode对象列表
     */
    List<SqlNode> parseDynamicTags(Element element) {
        List<SqlNode> contents = new ArrayList<>();
        // element.getText 拿到 SQL
        String data = element.getText();
        contents.add(new StaticTextSqlNode(data));
        return contents;
    }
}
