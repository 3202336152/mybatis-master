package com.huanyu.mybatis;

import com.alibaba.fastjson.JSON;
import com.huanyu.mybatis.binding.MapperRegistry;
import com.huanyu.mybatis.dao.IUserDao;
import com.huanyu.mybatis.io.Resources;
import com.huanyu.mybatis.po.User;
import com.huanyu.mybatis.session.SqlSession;
import com.huanyu.mybatis.session.SqlSessionFactory;
import com.huanyu.mybatis.session.SqlSessionFactoryBuilder;
import com.huanyu.mybatis.session.defaults.DefaultSqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;

/**
 * ClassName: ApiTest
 * Package: com.huanyu.mybatis
 * Description: 单元测试
 *
 * @Author: 寰宇
 * @Create: 2024/6/11 16:07
 * @Version: 1.0
 */
public class ApiTest {

    private Logger logger = LoggerFactory.getLogger(ApiTest.class);

    // XML的解析和注册使用
    @Test
    public void test_SqlSessionFactory() throws IOException {
        // 1. 从SqlSessionFactory中获取SqlSession
        Reader reader = Resources.getResourceAsReader("mybatis-config-datasource.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().builder(reader);
        SqlSession sqlSession = sqlSessionFactory.openSession();

        // 2. 获取映射器对象
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);

        // 3. 测试验证
        User user = userDao.queryUserInfoById(1L);
        logger.info("测试结果：{}", JSON.toJSONString(user));

    }

}
