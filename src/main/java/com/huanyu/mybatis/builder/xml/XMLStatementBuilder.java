package com.huanyu.mybatis.builder.xml;

import com.huanyu.mybatis.builder.BaseBuilder;
import com.huanyu.mybatis.mapping.MappedStatement;
import com.huanyu.mybatis.mapping.SqlCommandType;
import com.huanyu.mybatis.mapping.SqlSource;
import com.huanyu.mybatis.scripting.LanguageDriver;
import com.huanyu.mybatis.session.Configuration;
import org.dom4j.Element;

import java.util.Locale;

/**
 * ClassName: XMLStatementBuilder
 * Package: com.huanyu.mybatis.builder.xml
 * Description: XML语句构建器
 * 单条语句的解析器，解析类似：
 * <select id="selectUser" resultType="com.example.demo.UserBean">
 *     select * from `user` where id = #{id}
 * </select>
 *
 * @Author: 寰宇
 * @Create: 2024/6/17 21:27
 * @Version: 1.0
 */
public class XMLStatementBuilder extends BaseBuilder {
    private String currentNamespace;
    private Element element;

    public XMLStatementBuilder(Configuration configuration, Element element, String currentNamespace) {
        super(configuration);
        this.element = element;
        this.currentNamespace = currentNamespace;
    }

    // 解析select、insert、update、delete这四类节点
    //<select
    //  id="selectPerson"
    //  parameterType="int"
    //  parameterMap="deprecated"
    //  resultType="hashmap"
    //  resultMap="personResultMap"
    //  flushCache="false"
    //  useCache="true"
    //  timeout="10000"
    //  fetchSize="256"
    //  statementType="PREPARED"
    //  resultSetType="FORWARD_ONLY">
    //  SELECT * FROM PERSON WHERE ID = #{id}
    //</select>
    public void parseStatementNode() {
        // 读取当前节点的id与databaseId
        String id = element.attributeValue("id");
        // 参数类型
        String parameterType = element.attributeValue("parameterType");
        Class<?> parameterTypeClass = resolveAlias(parameterType);
        // 结果类型
        String resultType = element.attributeValue("resultType");
        Class<?> resultTypeClass = resolveAlias(resultType);
        // 获取命令类型(select|insert|update|delete)
        // 读取节点名
        String nodeName = element.getName();
        // 读取和判断语句类型
        SqlCommandType sqlCommandType = SqlCommandType.valueOf(nodeName.toUpperCase(Locale.ENGLISH));

        // 获取默认语言驱动器
        Class<?> langClass = configuration.getLanguageRegistry().getDefaultDriverClass();
        LanguageDriver langDriver = configuration.getLanguageRegistry().getDriver(langClass);

        SqlSource sqlSource = langDriver.createSqlSource(configuration, element, parameterTypeClass);

        MappedStatement mappedStatement = new MappedStatement.Builder(configuration, currentNamespace + "." + id, sqlCommandType, sqlSource, resultTypeClass).build();

        // 添加解析 SQL
        configuration.addMappedStatement(mappedStatement);
    }
}
