package com.huanyu.mybatis;

import com.alibaba.fastjson.JSON;
import com.huanyu.mybatis.dao.IActivityDao;
import com.huanyu.mybatis.io.Resources;
import com.huanyu.mybatis.po.Activity;
import com.huanyu.mybatis.session.SqlSession;
import com.huanyu.mybatis.session.SqlSessionFactory;
import com.huanyu.mybatis.session.SqlSessionFactoryBuilder;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

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

    private SqlSession sqlSession;

//    @Before
//    public void init() throws IOException {
//        // 1. 从SqlSessionFactory中获取SqlSession
//        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().builder(Resources.getResourceAsReader("mybatis-config-datasource.xml"));
//        sqlSession = sqlSessionFactory.openSession();
//    }

    @Test
    public void test_queryActivityById() throws IOException {
        // 1. 从SqlSessionFactory中获取SqlSession
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().builder(Resources.getResourceAsReader("mybatis-config-datasource.xml"));
        sqlSession = sqlSessionFactory.openSession();

        // 2. 获取映射器对象
        IActivityDao dao = sqlSession.getMapper(IActivityDao.class);
        // 3. 测试验证
        Activity req = new Activity();
        req.setActivityId(100001L);
//        Activity res = dao.queryActivityById(req);
//        logger.info("测试结果：{}", JSON.toJSONString(res));

        logger.info("测试结果：{}", JSON.toJSONString(dao.queryActivityById(req)));

//         sqlSession.commit();
//         sqlSession.clearCache();
//         sqlSession.close();

        logger.info("测试结果：{}", JSON.toJSONString(dao.queryActivityById(req)));
    }

    @Test
    public void test_ognl() throws OgnlException {
        Activity req = new Activity();
        req.setActivityId(1L);
        req.setActivityName("测试活动");
        req.setActivityDesc("寰宇的测试内容");

        OgnlContext context = new OgnlContext();
        context.setRoot(req);
        Object root = context.getRoot();

        Object activityName = Ognl.getValue("activityName", context, root);
        Object activityDesc = Ognl.getValue("activityDesc", context, root);
        Object value = Ognl.getValue("activityDesc.length()", context, root);

        System.out.println(activityName + "\t" + activityDesc + " length：" + value);
    }

    @Test
    public void test_queryActivityByIdCun() throws IOException {
        // 1. 从SqlSessionFactory中获取SqlSession
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().builder(Resources.getResourceAsReader("mybatis-config-datasource.xml"));
        // 2. 请求对象
        Activity req = new Activity();
        req.setActivityId(100001L);

        // 3. 第一组：SqlSession
        // 3.1 开启 Session
        SqlSession sqlSession01 = sqlSessionFactory.openSession();
        // 3.2 获取映射器对象
        IActivityDao dao01 = sqlSession01.getMapper(IActivityDao.class);
        logger.info("测试结果01：{}", JSON.toJSONString(dao01.queryActivityById(req)));
        sqlSession01.commit();

        // 4. 第一组：SqlSession
        // 4.1 开启 Session
        SqlSession sqlSession02 = sqlSessionFactory.openSession();
        // 4.2 获取映射器对象
        IActivityDao dao02 = sqlSession02.getMapper(IActivityDao.class);
        logger.info("测试结果02：{}", JSON.toJSONString(dao02.queryActivityById(req)));


        sqlSession01.close();
        sqlSession02.close();
    }


}
