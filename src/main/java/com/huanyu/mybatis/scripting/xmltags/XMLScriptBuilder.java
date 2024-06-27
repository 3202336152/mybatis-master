package com.huanyu.mybatis.scripting.xmltags;

import com.huanyu.mybatis.builder.BaseBuilder;
import com.huanyu.mybatis.mapping.SqlSource;
import com.huanyu.mybatis.scripting.defaults.RawSqlSource;
import com.huanyu.mybatis.session.Configuration;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    // 节点类型和对应的处理器组成的Map
    private final Map<String, NodeHandler> nodeHandlerMap = new HashMap<>();

    public XMLScriptBuilder(Configuration configuration, Element element, Class<?> parameterType) {
        super(configuration);
        this.element = element;
        this.parameterType = parameterType;
        initNodeHandlerMap();
    }

    private void initNodeHandlerMap() {
        // 9种：trim/where/set/foreach/if/choose/when/otherwise/bind
        nodeHandlerMap.put("trim", new TrimHandler());
        nodeHandlerMap.put("if", new IfHandler());
    }

    /**
     * 解析节点生成SqlSource对象
     * @return SqlSource对象
     */
    public SqlSource parseScriptNode() {
        // 解析XML节点节点，得到节点树MixedSqlNode
        List<SqlNode> contents = parseDynamicTags(element);
        MixedSqlNode rootSqlNode = new MixedSqlNode(contents);
        SqlSource sqlSource = null;
        if (isDynamic) {
            sqlSource = new DynamicSqlSource(configuration, rootSqlNode);
        } else {
            sqlSource = new RawSqlSource(configuration, rootSqlNode, parameterType);
        }
        return sqlSource;
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
        List<SqlNode> contents = new ArrayList<>(); // 用于存储解析后的SqlNode对象列表
        List<Node> children = element.content(); // 获取动态标签的子节点
        for (Node child : children) { // 遍历每个子节点
            if (child.getNodeType() == Node.TEXT_NODE || child.getNodeType() == Node.CDATA_SECTION_NODE) {
                String data = child.getText(); // 获取文本内容
                TextSqlNode textSqlNode = new TextSqlNode(data); // 创建TextSqlNode对象
                 if (textSqlNode.isDynamic()) { // 检查是否为动态SQL片段
                    contents.add(textSqlNode); // 添加到内容列表
                    isDynamic = true; // 标记为动态SQL
                } else {
                    contents.add(new StaticTextSqlNode(data)); // 如果不是动态SQL，则作为静态SQL处理
                }
            } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                String nodeName = child.getName(); // 获取子节点的名称
                NodeHandler handler = nodeHandlerMap.get(nodeName); // 获取对应的NodeHandler
                if (handler == null) {
                    throw new RuntimeException("Unknown element <" + nodeName + "> in SQL statement."); // 未知的元素节点，抛出异常
                }
                // 用处理器处理节点
                handler.handleNode(element.element(child.getName()), contents);
                isDynamic = true; // 标记为动态SQL
            }
        }
        return contents; // 返回解析后的SqlNode对象列表
    }

    // NodeHandler接口，用于处理特定类型的节点
    private interface NodeHandler {
        /**
         * 该方法将当前节点拼装到节点树中
         * @param nodeToHandle 要被拼接的节点
         * @param targetContents 节点树
         */
        void handleNode(Element nodeToHandle, List<SqlNode> targetContents);
    }

    // TrimHandler类，实现NodeHandler接口，用于处理<trim>节点
    private class TrimHandler implements NodeHandler {
        @Override
        public void handleNode(Element nodeToHandle, List<SqlNode> targetContents) {
            List<SqlNode> contents = parseDynamicTags(nodeToHandle); // 递归解析<trim>节点的子节点
            MixedSqlNode mixedSqlNode = new MixedSqlNode(contents); // 创建MixedSqlNode对象
            String prefix = nodeToHandle.attributeValue("prefix"); // 获取prefix属性
            String prefixOverrides = nodeToHandle.attributeValue("prefixOverrides"); // 获取prefixOverrides属性
            String suffix = nodeToHandle.attributeValue("suffix"); // 获取suffix属性
            String suffixOverrides = nodeToHandle.attributeValue("suffixOverrides"); // 获取suffixOverrides属性
            TrimSqlNode trim = new TrimSqlNode(configuration, mixedSqlNode, prefix, prefixOverrides, suffix, suffixOverrides); // 创建TrimSqlNode对象
            targetContents.add(trim); // 将TrimSqlNode对象添加到目标内容列表
        }
    }

    // IfHandler类，实现NodeHandler接口，用于处理<if>节点
    private class IfHandler implements NodeHandler {
        @Override
        public void handleNode(Element nodeToHandle, List<SqlNode> targetContents) {
            List<SqlNode> contents = parseDynamicTags(nodeToHandle); // 递归解析<if>节点的子节点
            MixedSqlNode mixedSqlNode = new MixedSqlNode(contents); // 创建MixedSqlNode对象
            String test = nodeToHandle.attributeValue("test"); // 获取test属性
            IfSqlNode ifSqlNode = new IfSqlNode(mixedSqlNode, test); // 创建IfSqlNode对象
            targetContents.add(ifSqlNode); // 将IfSqlNode对象添加到目标内容列表
        }
    }

}
