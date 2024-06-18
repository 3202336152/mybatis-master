package com.huanyu.mybatis.mapping;

/**
 * ClassName: SqlSource
 * Package: com.huanyu.mybatis.mapping
 * Description: SQL源码
 * 表示从 XML 文件或注释中读取的映射语句的内容
 * 它根据从用户收到的输入参数创建将传递给数据库的 SQL
 * @Author: 寰宇
 * @Create: 2024/6/17 21:30
 * @Version: 1.0
 */
public interface SqlSource {

    /**
     * 获取一个BoundSql对象
     * @param parameterObject 参数对象
     * @return BoundSql对象
     */
    BoundSql getBoundSql(Object parameterObject);
}
