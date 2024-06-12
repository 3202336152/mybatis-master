package com.huanyu.mybatis.session;

import com.huanyu.mybatis.builder.xml.XMLConfigBuilder;
import com.huanyu.mybatis.session.defaults.DefaultSqlSessionFactory;

import java.io.Reader;

/**
 * ClassName: SqlSessionFactoryBuilder
 * Package: com.huanyu.mybatis.session
 * Description: 构建SqlSessionFactory的工厂
 *
 * @Author: 寰宇
 * @Create: 2024/6/12 14:46
 * @Version: 1.0
 */
public class SqlSessionFactoryBuilder {

    /**
     * 建造一个SqlSessionFactory对象
     * @param reader 读取字符流的抽象类
//     * @param environment 环境信息
//     * @param properties 配置信息
     * @return SqlSessionFactory对象
     */
    public SqlSessionFactory builder(Reader reader) {
        // 传入配置文件，创建一个XMLConfigBuilder类
        XMLConfigBuilder xmlConfigBuilder = new XMLConfigBuilder(reader);
        // 分两步：
        // 1、解析配置文件，得到配置文件对应的Configuration对象
        // 2、根据Configuration对象，获得一个DefaultSqlSessionFactory
        return build(xmlConfigBuilder.parse());
    }

    /**
     * 根据配置信息建造一个SqlSessionFactory对象
     * @param config 配置信息
     * @return SqlSessionFactory对象
     */
    public SqlSessionFactory build(Configuration config) {
        // 使用传入的Configuration对象创建一个DefaultSqlSessionFactory对象
        return new DefaultSqlSessionFactory(config);
    }
}
