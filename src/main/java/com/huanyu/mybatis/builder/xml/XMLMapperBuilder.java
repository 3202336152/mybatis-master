package com.huanyu.mybatis.builder.xml;

import com.huanyu.mybatis.builder.BaseBuilder;
import com.huanyu.mybatis.builder.MapperBuilderAssistant;
import com.huanyu.mybatis.io.Resources;
import com.huanyu.mybatis.session.Configuration;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
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

        // 2.配置select|insert|update|delete
        // 处理各个数据库操作语句
        buildStatementFromContext(element.elements("select"),
                element.elements("insert"),
                element.elements("update"),
                element.elements("delete")
        );
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
