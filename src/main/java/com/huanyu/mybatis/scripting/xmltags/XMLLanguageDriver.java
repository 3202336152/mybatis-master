package com.huanyu.mybatis.scripting.xmltags;

import com.huanyu.mybatis.executor.parameter.ParameterHandler;
import com.huanyu.mybatis.mapping.BoundSql;
import com.huanyu.mybatis.mapping.MappedStatement;
import com.huanyu.mybatis.mapping.SqlSource;
import com.huanyu.mybatis.scripting.LanguageDriver;
import com.huanyu.mybatis.scripting.defaults.DefaultParameterHandler;
import com.huanyu.mybatis.session.Configuration;
import org.dom4j.Element;

/**
 * ClassName: XMLLanguageDriver
 * Package: com.huanyu.mybatis.scripting.defaults.xmltags
 * Description: XML语言驱动器
 *
 * @Author: 寰宇
 * @Create: 2024/6/17 21:51
 * @Version: 1.0
 */
public class XMLLanguageDriver implements LanguageDriver {

    /**
     * 创建SqlSource对象（基于映射文件的方式）。该方法在MyBatis启动阶段，读取映射接口或映射文件时被调用
     * @param configuration 配置信息
     * @param script 映射文件中的数据库操作节点
     * @param parameterType 参数类型
     * @return SqlSource对象
     */
    @Override
    public SqlSource createSqlSource(Configuration configuration, Element script, Class<?> parameterType) {
        // 用XML脚本构建器解析
        XMLScriptBuilder builder = new XMLScriptBuilder(configuration, script, parameterType);
        return builder.parseScriptNode();
    }

    @Override
    public ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        return new DefaultParameterHandler(mappedStatement, parameterObject, boundSql);
    }
}
