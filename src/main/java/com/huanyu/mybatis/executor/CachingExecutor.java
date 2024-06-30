package com.huanyu.mybatis.executor;

import com.alibaba.fastjson.JSON;
import com.huanyu.mybatis.cache.Cache;
import com.huanyu.mybatis.cache.CacheKey;
import com.huanyu.mybatis.cache.TransactionalCacheManager;
import com.huanyu.mybatis.mapping.BoundSql;
import com.huanyu.mybatis.mapping.MappedStatement;
import com.huanyu.mybatis.session.ResultHandler;
import com.huanyu.mybatis.session.RowBounds;
import com.huanyu.mybatis.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

/**
 * ClassName: CachingExecutor
 * Package: com.huanyu.mybatis.executor
 * Description: 二级缓存执行器
 *
 * @Author: 寰宇
 * @Create: 2024/6/29 15:39
 * @Version: 1.0
 */
public class CachingExecutor implements Executor {

    private Logger logger = LoggerFactory.getLogger(CachingExecutor.class);

    // 被装饰的执行器
    private Executor delegate;
    // 事务缓存管理器
    private TransactionalCacheManager tcm = new TransactionalCacheManager();

    public CachingExecutor(Executor delegate) {
        this.delegate = delegate;
        delegate.setExecutorWrapper(this);
    }

    /**
     * 更新数据库数据，INSERT/UPDATE/DELETE三种操作都会调用该方法
     * @param ms 映射语句
     * @param parameter 参数对象
     * @return 数据库操作结果
     * @throws SQLException
     */
    @Override
    public int update(MappedStatement ms, Object parameter) throws SQLException {
        return delegate.update(ms, parameter);
    }

    /**
     * 查询数据库中的数据
     * @param ms 映射语句
     * @param parameter 参数对象
     * @param rowBounds 翻页限制条件
     * @param resultHandler 结果处理器
     * @param key 缓存的键
     * @param boundSql 查询语句
     * @param <E> 结果类型
     * @return 结果列表
     * @throws SQLException
     */
    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
        // 获取当前语句的缓存对象
        Cache cache = ms.getCache();
        if (cache != null) {
            // 如果需要的话，刷新缓存
            flushCacheIfRequired(ms);
            // 如果配置了使用缓存，并且没有指定结果处理器
            if (ms.isUseCache() && resultHandler == null) {
                // 尝试从缓存中获取数据
                @SuppressWarnings("unchecked")
                List<E> list = (List<E>) tcm.getObject(cache, key);
                if (list == null) {
                    // 如果缓存中没有数据，则执行实际的查询操作
                    list = delegate.<E>query(ms, parameter, rowBounds, resultHandler, key, boundSql);
                    // cache：缓存队列实现类，FIFO
                    // key：哈希值 [mappedStatementId + offset + limit + SQL + queryParams + environment]
                    // list：查询的数据
                    // 将查询结果放入缓存中
                    tcm.putObject(cache, key, list);
                }
                // 如果日志级别为调试模式，并且缓存中有数据，则打印调试日志
                if (logger.isDebugEnabled() && cache.getSize() > 0) {
                    logger.debug("二级缓存：{}", JSON.toJSONString(list));
                }
                return list; // 返回查询结果
            }
        }
        // 如果不使用缓存或者结果处理器不为 null，则直接执行查询并返回结果
        return delegate.<E>query(ms, parameter, rowBounds, resultHandler, key, boundSql);
    }


    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
        // 1. 获取绑定SQL
        BoundSql boundSql = ms.getBoundSql(parameter);
        // 2. 创建缓存Key
        CacheKey key = createCacheKey(ms, parameter, rowBounds, boundSql);
        return query(ms, parameter, rowBounds, resultHandler, key, boundSql);
    }

    @Override
    public Transaction getTransaction() {
        return delegate.getTransaction();
    }

    @Override
    public void commit(boolean required) throws SQLException {
        delegate.commit(required);
        tcm.commit();
    }

    @Override
    public void rollback(boolean required) throws SQLException {
        try {
            delegate.rollback(required);
        } finally {
            if (required) {
                tcm.rollback();
            }
        }
    }

    @Override
    public void close(boolean forceRollback) {
        try {
            if (forceRollback) {
                tcm.rollback();
            } else {
                tcm.commit();
            }
        } finally {
            delegate.close(forceRollback);
        }
    }

    @Override
    public void clearLocalCache() {
        delegate.clearLocalCache();
    }

    @Override
    public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
        return delegate.createCacheKey(ms, parameterObject, rowBounds, boundSql);
    }

    @Override
    public void setExecutorWrapper(Executor executor) {
        throw new UnsupportedOperationException("This method should not be called");
    }

    private void flushCacheIfRequired(MappedStatement ms) {
        Cache cache = ms.getCache();
        if (cache != null && ms.isFlushCacheRequired()) {
            tcm.clear(cache);
        }
    }
}
