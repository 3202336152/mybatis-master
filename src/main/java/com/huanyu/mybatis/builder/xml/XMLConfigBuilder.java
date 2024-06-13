package com.huanyu.mybatis.builder.xml;

import com.huanyu.mybatis.builder.BaseBuilder;
import com.huanyu.mybatis.datasource.DataSourceFactory;
import com.huanyu.mybatis.io.Resources;
import com.huanyu.mybatis.mapping.BoundSql;
import com.huanyu.mybatis.mapping.Environment;
import com.huanyu.mybatis.mapping.MappedStatement;
import com.huanyu.mybatis.mapping.SqlCommandType;
import com.huanyu.mybatis.session.Configuration;
import com.huanyu.mybatis.transaction.TransactionFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

import javax.sql.DataSource;
import java.io.Reader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ClassName: XMLConfigBuilder
 * Package: com.huanyu.mybatis.builder.xml
 * Description: XML配置构建器，建造者模式，继承BaseBuilder
 *
 * @Author: 寰宇
 * @Create: 2024/6/12 14:48
 * @Version: 1.0
 */
public class XMLConfigBuilder extends BaseBuilder {
    private Element root;

    public XMLConfigBuilder(Reader reader) {
        // 1. 调用父类初始化Configuration
        super(new Configuration());
        // 2. dom4j 处理 xml
        SAXReader saxReader = new SAXReader();
        try {
            Document document = saxReader.read(new InputSource(reader));
            root = document.getRootElement();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析配置；类型别名、插件、对象工厂、对象包装工厂、设置、环境、类型转换、映射器
     *
     * @return Configuration
     */
    public Configuration parse() {
        try {
            // 环境
            environmentsElement(root.element("environments"));
            // 解析映射器
            mapperElement(root.element("mappers"));
        } catch (Exception e) {
            throw new RuntimeException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
        }
        return configuration;
    }

    /**
     * <environments default="development">
     * <environment id="development">
     * <transactionManager type="JDBC">
     * <property name="..." value="..."/>
     * </transactionManager>
     * <dataSource type="POOLED">
     * <property name="driver" value="${driver}"/>
     * <property name="url" value="${url}"/>
     * <property name="username" value="${username}"/>
     * <property name="password" value="${password}"/>
     * </dataSource>
     * </environment>
     * </environments>
     */
    private void environmentsElement(Element context) throws InstantiationException, IllegalAccessException {
        // 从context元素的“default”属性中获取默认环境ID
        String environment = context.attributeValue("default");

        // 获取context元素内的所有“environment”元素
        List<Element> environmentList = context.elements("environment");
        for (Element e : environmentList) {
            // 获取当前“environment”元素的ID
            String id = e.attributeValue("id");

            // 检查当前环境是否与默认环境匹配
            if (environment.equals(id)) {
                // 初始化事务管理器
                TransactionFactory txFactory = (TransactionFactory) typeAliasRegistry
                        .resolveAlias(e.element("transactionManager").attributeValue("type"))
                        .newInstance();

                // 初始化数据源
                Element dataSourceElement = e.element("dataSource");
                DataSourceFactory dataSourceFactory = (DataSourceFactory) typeAliasRegistry
                        .resolveAlias(dataSourceElement.attributeValue("type"))
                        .newInstance();

                // 设置数据源的属性
                List<Element> propertyList = dataSourceElement.elements("property");
                Properties props = new Properties();
                for (Element property : propertyList) {
                    props.setProperty(property.attributeValue("name"), property.attributeValue("value"));
                }
                dataSourceFactory.setProperties(props);

                // 获取配置好的数据源
                DataSource dataSource = dataSourceFactory.getDataSource();

                // 使用事务管理器和数据源构建环境
                Environment.Builder environmentBuilder = new Environment.Builder(id)
                        .transactionFactory(txFactory)
                        .dataSource(dataSource);

                // 将构建好的环境设置到配置中
                configuration.setEnvironment(environmentBuilder.build());
            }
        }
    }


    /**
     * 解析mappers节点，例如：
     * <mappers>
     *    <mapper resource="com/huanyu/mybatis/dao//UserDao.xml"/>
     *    <package name="com.huanyu.mybatis.mybatisDemo" />
     * </mappers>
     * @param mappers mappers节点
     * @throws Exception
     */
    private void mapperElement(Element mappers) throws Exception {
        // 获取所有“mapper”元素
        List<Element> mapperList = mappers.elements("mapper");
        for (Element e : mapperList) {
            // 获取“resource”属性的值
            String resource = e.attributeValue("resource");
            // 使用资源路径获取Reader
            Reader reader = Resources.getResourceAsReader(resource);
            // 使用SAXReader解析XML文档
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(new InputSource(reader));
            Element root = document.getRootElement();
            // 解析命名空间
            String namespace = root.attributeValue("namespace");

            // 解析SELECT语句
            List<Element> selectNodes = root.elements("select");
            for (Element node : selectNodes) {
                String id = node.attributeValue("id");
                String parameterType = node.attributeValue("parameterType");
                String resultType = node.attributeValue("resultType");
                String sql = node.getText();

                // 匹配参数并替换为问号
                Map<Integer, String> parameter = new HashMap<>();
                Pattern pattern = Pattern.compile("(#\\{(.*?)})");
                Matcher matcher = pattern.matcher(sql);
                for (int i = 1; matcher.find(); i++) {
                    String g1 = matcher.group(1);
                    String g2 = matcher.group(2);
                    parameter.put(i, g2);
                    sql = sql.replace(g1, "?");
                }

                // 生成SQL命令ID
                String msId = namespace + "." + id;
                // 获取SQL命令类型
                String nodeName = node.getName();
                SqlCommandType sqlCommandType = SqlCommandType.valueOf(nodeName.toUpperCase(Locale.ENGLISH));

                // 创建BoundSql对象
                BoundSql boundSql = new BoundSql(sql, parameter, parameterType, resultType);

                // 创建并构建MappedStatement对象
                MappedStatement mappedStatement = new MappedStatement.Builder(configuration, msId, sqlCommandType, boundSql).build();
                // 添加解析的SQL到配置中
                configuration.addMappedStatement(mappedStatement);
            }

            // 注册Mapper映射器
            configuration.addMapper(Resources.classForName(namespace));
        }
    }

}
