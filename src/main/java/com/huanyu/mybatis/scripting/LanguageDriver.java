package com.huanyu.mybatis.scripting;

import com.huanyu.mybatis.executor.parameter.ParameterHandler;
import com.huanyu.mybatis.mapping.BoundSql;
import com.huanyu.mybatis.mapping.MappedStatement;
import com.huanyu.mybatis.mapping.SqlSource;
import com.huanyu.mybatis.session.Configuration;
import org.dom4j.Element;

/**
 * ClassName: LanguageDriver
 * Package: com.huanyu.mybatis.scripting
 * Description: 脚本语言驱动
 * 在接口上注解的SQL语句，就是由它进行解析的
 * _@Select("select * from `user` where id = #{id}")
 * User queryUserById(Integer id);
 * @Author: 寰宇
 * @Create: 2024/6/17 21:40
 * @Version: 1.0
 */
public interface LanguageDriver {

    /**
     * 创建SqlSource对象（基于映射文件的方式）。该方法在MyBatis启动阶段，读取映射接口或映射文件时被调用
     * @param configuration 配置信息
     * @param script 映射文件中的数据库操作节点
     * @param parameterType 参数类型
     * @return SqlSource对象
     */
    SqlSource createSqlSource(Configuration configuration, Element script, Class<?> parameterType);

    /**
     * 创建SQL源码(annotation 注解方式)
     */
    SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType);

    /**
     * 创建参数处理器
     */
    ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql);


}
