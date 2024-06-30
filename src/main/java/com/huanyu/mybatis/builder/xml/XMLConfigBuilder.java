package com.huanyu.mybatis.builder.xml;

import com.huanyu.mybatis.builder.BaseBuilder;
import com.huanyu.mybatis.datasource.DataSourceFactory;
import com.huanyu.mybatis.io.Resources;
import com.huanyu.mybatis.mapping.Environment;
import com.huanyu.mybatis.plugin.Interceptor;
import com.huanyu.mybatis.session.Configuration;
import com.huanyu.mybatis.session.LocalCacheScope;
import com.huanyu.mybatis.transaction.TransactionFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.Properties;

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
            // 插件
            pluginElement(root.element("plugins"));
            // 设置
            settingsElement(root.element("settings"));
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
     * Mybatis 允许你在某一点切入映射语句执行的调度
     * <plugins>
     *     <plugin interceptor="com.huanyu.mybatis.plugin.TestPlugin">
     *         <property name="test00" value="100"/>
     *         <property name="test01" value="100"/>
     *     </plugin>
     * </plugins>
     */
    private void pluginElement(Element parent) throws Exception {
        if (parent == null) return; // 如果父元素为空，直接返回

        List<Element> elements = parent.elements(); // 获取父元素的所有子元素
        for (Element element : elements) { // 遍历每个子元素
            String interceptor = element.attributeValue("interceptor"); // 获取子元素中 "interceptor" 属性的值

            // 参数配置
            Properties properties = new Properties(); // 创建一个Properties对象用于存储属性
            List<Element> propertyElementList = element.elements("property"); // 获取子元素中所有名为 "property" 的子元素
            for (Element property : propertyElementList) { // 遍历每个 "property" 子元素
                properties.setProperty(property.attributeValue("name"), property.attributeValue("value")); // 将每个 "property" 子元素的 "name" 和 "value" 属性值存储到Properties对象中
            }
            // 获取插件实现类并实例化
            // 这里假设 "interceptor" 属性值是实现类的完全限定名，例如："com.huanyu.mybatis.plugin.TestPlugin"
            Interceptor interceptorInstance = (Interceptor) resolveClass(interceptor).newInstance(); // 通过反射创建Interceptor实例
            interceptorInstance.setProperties(properties); // 将配置属性设置到Interceptor实例中
            configuration.addInterceptor(interceptorInstance); // 将Interceptor实例添加到配置中
        }
    }

    /**
     * <settings>
     * <!-- 全局缓存：true/false -->
     * <setting name="cacheEnabled" value="false"/>
     * <!--缓存级别：SESSION/STATEMENT-->
     * <setting name="localCacheScope" value="SESSION"/>
     * </settings>
     */
    private void settingsElement(Element context) {
        // 如果context为null，则直接返回
        if (context == null) return;
        // 获取context的所有子元素，并存储在一个List中
        List<Element> elements = context.elements();
        // 创建一个Properties对象来存储属性键值对
        Properties props = new Properties();
        // 遍历所有子元素
        for (Element element : elements) {
            // 将每个子元素的"name"属性和"value"属性添加到Properties对象中
            props.setProperty(element.attributeValue("name"), element.attributeValue("value"));
        }
        // 根据配置文件设置缓存是否启用
        configuration.setCacheEnabled(booleanValueOf(props.getProperty("cacheEnabled"), true));
        // 根据Properties对象中的"localCacheScope"属性值，设置本地缓存范围
        configuration.setLocalCacheScope(LocalCacheScope.valueOf(props.getProperty("localCacheScope")));
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
     *	 <mapper resource="com/huanyu/mybatis/dao/IUserDao.xml"/>
     *	 <mapper resource="com/huanyu/mybatis/dao/BlogMapper.xml"/>
     *	 <mapper resource="com/huanyu/mybatis/dao/PostMapper.xml"/>
     *   <mapper class="com.huanyu.mybatis.dao.IUserDao"/>
     * </mappers>
     * @param mappers mappers节点
     * @throws Exception
     */
    private void mapperElement(Element mappers) throws Exception {
        // 获取所有“mapper”元素
        List<Element> mapperList = mappers.elements("mapper");
        for (Element e : mapperList) {
            String resource = e.attributeValue("resource");
            String mapperClass = e.attributeValue("class");
            // XML 解析
            if (resource != null && mapperClass == null) {
                InputStream inputStream = Resources.getResourceAsStream(resource);
                // 在for循环里每个mapper都重新new一个XMLMapperBuilder，来解析
                XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, resource);
                mapperParser.parse();
            }
            // Annotation 注解解析
            else if (resource == null && mapperClass != null) {
                Class<?> mapperInterface = Resources.classForName(mapperClass);
                configuration.addMapper(mapperInterface);
            }
        }
    }

}
