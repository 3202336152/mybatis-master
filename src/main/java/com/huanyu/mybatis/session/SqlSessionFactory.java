package com.huanyu.mybatis.session;

/**
 * ClassName: SqlSessionFactory
 * Package: com.huanyu.mybatis.session
 * Description: 工厂模式接口，构建SqlSession的工厂
 * 通过连接或数据源创建一个 {@link SqlSession}
 * @Author: 寰宇
 * @Create: 2024/6/11 15:59
 * @Version: 1.0
 */
public interface SqlSessionFactory {

    /**
     * 打开一个 session
     * @return SqlSession
     */
    SqlSession openSession();
}
