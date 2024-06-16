package com.huanyu.mybatis.session.defaults;

import com.huanyu.mybatis.executor.Executor;
import com.huanyu.mybatis.mapping.Environment;
import com.huanyu.mybatis.session.Configuration;
import com.huanyu.mybatis.session.SqlSession;
import com.huanyu.mybatis.session.SqlSessionFactory;
import com.huanyu.mybatis.session.TransactionIsolationLevel;
import com.huanyu.mybatis.transaction.Transaction;
import com.huanyu.mybatis.transaction.TransactionFactory;

import java.sql.SQLException;

/**
 * ClassName: DefaultSqlSessionFactory
 * Package: com.huanyu.mybatis.session.defaults
 * Description: 默认的SqlSessionFactory实现类
 *
 * @Author: 寰宇
 * @Create: 2024/6/11 16:00
 * @Version: 1.0
 */
public class DefaultSqlSessionFactory implements SqlSessionFactory {

    private final Configuration configuration;

    public DefaultSqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    // 默认的简单工厂实现，
    // 处理开启 SqlSession 时，对 DefaultSqlSession 的创建以及传递 mapperRegistry，
    // 这样就可以在使用 SqlSession 时获取每个代理类的映射器对象。
    @Override
    public SqlSession openSession() {
        Transaction tx = null;
        try {
            final Environment environment = configuration.getEnvironment();
            TransactionFactory transactionFactory = environment.getTransactionFactory();
            tx = transactionFactory.newTransaction(configuration.getEnvironment().getDataSource(), TransactionIsolationLevel.READ_COMMITTED, false);
            // 创建执行器
            final Executor executor = configuration.newExecutor(tx);
            // 创建DefaultSqlSession
            return new DefaultSqlSession(configuration, executor);
        } catch (Exception e) {
            try {
                assert tx != null;
                tx.close();
            } catch (SQLException ignore) {
            }
            throw new RuntimeException("Error opening session.  Cause: " + e);
        }
    }
}
