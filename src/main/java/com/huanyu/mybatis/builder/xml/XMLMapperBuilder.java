package com.huanyu.mybatis.builder.xml;

import com.huanyu.mybatis.builder.BaseBuilder;
import com.huanyu.mybatis.builder.MapperBuilderAssistant;
import com.huanyu.mybatis.builder.ResultMapResolver;
import com.huanyu.mybatis.io.Resources;
import com.huanyu.mybatis.mapping.ResultFlag;
import com.huanyu.mybatis.mapping.ResultMap;
import com.huanyu.mybatis.mapping.ResultMapping;
import com.huanyu.mybatis.session.Configuration;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ClassName: XMLMapperBuilder
 * Package: com.huanyu.mybatis.builder.xml
 * Description: XML映射构建器
 * 这是用来解析Mapper文件的建造者
 * <mappers>
 *    <mapper resource="com/huanyu/mybatis/UserMapper.xml"/>
 * </mappers>
 *     中UserMapper.xml就是由他建造到configuration的mapper中的
 *     即，它处理：
 * <mapper namespace="com.example.demo.UserDao">
 *   <cache eviction="FIFO" flushInterval="60000"/>
 *   <select id="selectUser" resultType="com.example.demo.UserBean">    -- 对应xxxx，由XMLMapperBuilder解析
 *       select * from `user` where id = #{id}
 *   </select>
 * </mapper>
 * @Author: 寰宇
 * @Create: 2024/6/17 21:19
 * @Version: 1.0
 */
public class XMLMapperBuilder extends BaseBuilder {

    private Element element;
    private String resource;
    // 映射器构建助手
    private MapperBuilderAssistant builderAssistant;

    public XMLMapperBuilder(InputStream inputStream, Configuration configuration, String resource) throws DocumentException {
        this(new SAXReader().read(inputStream), configuration, resource);
    }

    private XMLMapperBuilder(Document document, Configuration configuration, String resource) {
        super(configuration);
        this.builderAssistant = new MapperBuilderAssistant(configuration, resource);
        this.element = document.getRootElement();
        this.resource = resource;
    }

    /**
     * 解析Mapper文件
     */
    public void parse() throws Exception {
        // 如果当前资源没有加载过再加载，防止重复加载
        if (!configuration.isResourceLoaded(resource)) {
            configurationElement(element);
            // 标记一下，已经加载过了
            configuration.addLoadedResource(resource);
            // 绑定映射器到namespace
            configuration.addMapper(Resources.classForName(builderAssistant.getCurrentNamespace()));
        }
    }

    // 配置mapper元素
    // <mapper namespace="org.mybatis.example.BlogMapper">
    //   <select id="selectBlog" parameterType="int" resultType="Blog">
    //    select * from Blog where id = #{id}
    //   </select>
    // </mapper>
    private void configurationElement(Element element) {
        // 1.配置namespace
        String namespace = element.attributeValue("namespace");
        if (namespace.equals("")) {
            throw new RuntimeException("Mapper's namespace cannot be empty");
        }
        builderAssistant.setCurrentNamespace(namespace);

        // 2.解析resultMap
        resultMapElements(element.elements("resultMap"));

        // 3.配置select|insert|update|delete
        // 处理各个数据库操作语句
        buildStatementFromContext(element.elements("select"),
                element.elements("insert"),
                element.elements("update"),
                element.elements("delete")
        );
    }

    // 遍历给定的元素列表，并调用resultMapElement处理每个元素
    private void resultMapElements(List<Element> list) {
        for (Element element : list) { // 遍历元素列表
            try {
                // 调用resultMapElement处理每个元素，传递一个空的ResultMapping列表
                resultMapElement(element, Collections.emptyList());
            } catch (Exception ignore) { // 忽略异常
            }
        }
    }

    /**
     * <resultMap id="activityMap" type="com.huanyu.mybatis.po.Activity">
     * <id column="id" property="id"/>
     * <result column="activity_id" property="activityId"/>
     * <result column="activity_name" property="activityName"/>
     * <result column="activity_desc" property="activityDesc"/>
     * <result column="create_time" property="createTime"/>
     * <result column="update_time" property="updateTime"/>
     * </resultMap>
     */
    // 处理单个resultMap节点，将其解析为ResultMap对象
    private ResultMap resultMapElement(Element resultMapNode, List<ResultMapping> additionalResultMappings) throws Exception {
        // 获取resultMap节点的id属性
        String id = resultMapNode.attributeValue("id");
        // 获取resultMap节点的type属性
        String type = resultMapNode.attributeValue("type");
        // 将type属性解析为Class对象
        Class<?> typeClass = resolveClass(type);

        // 创建ResultMapping列表
        List<ResultMapping> resultMappings = new ArrayList<>();
        // 将额外的ResultMapping添加到列表中
        resultMappings.addAll(additionalResultMappings);

        // 获取resultMap节点的子元素列表
        List<Element> resultChildren = resultMapNode.elements();
        for (Element resultChild : resultChildren) { // 遍历子元素
            List<ResultFlag> flags = new ArrayList<>(); // 创建ResultFlag列表
            if ("id".equals(resultChild.getName())) { // 如果子元素名称是id
                flags.add(ResultFlag.ID); // 将ID标志添加到标志列表
            }
            // 构建并添加ResultMapping对象
            resultMappings.add(buildResultMappingFromContext(resultChild, typeClass, flags));
        }

        // 创建结果映射解析器
        ResultMapResolver resultMapResolver = new ResultMapResolver(builderAssistant, id, typeClass, resultMappings);
        return resultMapResolver.resolve(); // 解析ResultMap对象并返回
    }

    /**
     * <id column="id" property="id"/>
     * <result column="activity_id" property="activityId"/>
     */
    // 从上下文元素构建ResultMapping对象
    private ResultMapping buildResultMappingFromContext(Element context, Class<?> resultType, List<ResultFlag> flags) throws Exception {
        String property = context.attributeValue("property"); // 获取context元素的property属性
        String column = context.attributeValue("column"); // 获取context元素的column属性
        // 使用builderAssistant构建并返回ResultMapping对象
        return builderAssistant.buildResultMapping(resultType, property, column, flags);
    }

    // 配置select|insert|update|delete
    @SafeVarargs
    private final void buildStatementFromContext(List<Element>... lists) {
        for (List<Element> list : lists) {
            for (Element element : list) {
                // 单条语句的解析器，解析类似：
                // <select id="selectUser" resultType="com.example.demo.UserBean">
                //    select * from `user` where id = #{id}
                //  </select>
                final XMLStatementBuilder statementParser = new XMLStatementBuilder(configuration, builderAssistant, element);
                statementParser.parseStatementNode();
            }
        }
    }
}
