package com.huanyu.mybatis.executor;

import com.huanyu.mybatis.cache.CacheKey;
import com.huanyu.mybatis.cache.impl.PerpetualCache;
import com.huanyu.mybatis.mapping.BoundSql;
import com.huanyu.mybatis.mapping.MappedStatement;
import com.huanyu.mybatis.mapping.ParameterMapping;
import com.huanyu.mybatis.reflection.MetaObject;
import com.huanyu.mybatis.session.Configuration;
import com.huanyu.mybatis.session.LocalCacheScope;
import com.huanyu.mybatis.session.ResultHandler;
import com.huanyu.mybatis.session.RowBounds;
import com.huanyu.mybatis.transaction.Transaction;
import com.huanyu.mybatis.type.TypeHandlerRegistry;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * ClassName: BaseExecutor
 * Package: com.huanyu.mybatis.executor
 * Description: 执行器抽象基类
 *
 * @Author: 寰宇
 * @Create: 2024/6/15 16:13
 * @Version: 1.0
 */
public abstract class BaseExecutor implements Executor {

    private org.slf4j.Logger logger = LoggerFactory.getLogger(BaseExecutor.class);

    protected Configuration configuration;
    protected Transaction transaction;
    protected Executor wrapper;

    // 本地缓存
    protected PerpetualCache localCache;

    private boolean closed;
    // 查询堆栈
    protected int queryStack = 0;

    protected BaseExecutor(Configuration configuration, Transaction transaction) {
        this.configuration = configuration;
        this.transaction = transaction;
        this.wrapper = this;
        this.localCache = new PerpetualCache("LocalCache");

    }

    /**
     * 执行查询操作
     * @param ms 映射语句对象
     * @param parameter 参数对象
     * @param rowBounds 翻页限制
     * @param resultHandler 结果处理器
     * @param <E> 输出结果类型
     * @return 查询结果
     * @throws SQLException
     */
    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
        // 1. 获取绑定SQL
        BoundSql boundSql = ms.getBoundSql(parameter);
        // 2. 创建缓存Key
        CacheKey key = createCacheKey(ms, parameter, rowBounds, boundSql);
        return query(ms, parameter, rowBounds, resultHandler, key, boundSql);
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
        if (closed) {
            // 执行器已经关闭
            throw new RuntimeException("Executor was closed.");
        }
        // 清理局部缓存，查询堆栈为0则清理。queryStack 避免递归调用清理
        if (queryStack == 0 && ms.isFlushCacheRequired()) {
            // 清除一级缓存
            clearLocalCache();
        }
        List<E> list;
        try {
            queryStack++;
            // 尝试从本地缓存获取结果
            list = resultHandler == null ? (List<E>) localCache.getObject(key) : null;
            if (list == null) {
                // 本地缓存没有结果，故需要查询数据库
                list = queryFromDatabase(ms, parameter, rowBounds, resultHandler, key, boundSql);
            }
        } finally {
            queryStack--;
        }
        if (queryStack == 0) {
            // 如果本地缓存的作用域为STATEMENT，则立刻清除本地缓存
            if (configuration.getLocalCacheScope() == LocalCacheScope.STATEMENT) {
                clearLocalCache();
            }
        }
        return list;
    }

    /**
     * 从数据库中查询结果
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
    private <E> List<E> queryFromDatabase(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
        List<E> list;
        // 向缓存中增加占位符，表示正在查询
        localCache.putObject(key, ExecutionPlaceholder.EXECUTION_PLACEHOLDER);
        try {
            list = doQuery(ms, parameter, rowBounds, resultHandler, boundSql);
        } finally {
            // 删除占位符
            localCache.removeObject(key);
        }
        // 将查询结果写入缓存
        localCache.putObject(key, list);
        return list;
    }

    /**
     * 生成查询的缓存的键
     * @param ms 映射语句对象
     * @param parameterObject 参数对象
     * @param rowBounds 翻页限制
     * @param boundSql 解析结束后的SQL语句
     * @return 生成的键值
     */
    @Override
    public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
        // 如果执行器已经关闭，则抛出异常
        if (closed) {
            throw new RuntimeException("Executor was closed.");
        }
        // 创建一个新的CacheKey对象
        CacheKey cacheKey = new CacheKey();
        // 更新CacheKey的值，添加MappedStatement的ID
        cacheKey.update(ms.getId());
        // 更新CacheKey的值，添加RowBounds的偏移量和限制
        cacheKey.update(rowBounds.getOffset());
        cacheKey.update(rowBounds.getLimit());
        // 更新CacheKey的值，添加BoundSql的SQL语句
        cacheKey.update(boundSql.getSql());
        // 获取BoundSql的参数映射
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        // 获取类型处理器注册表
        TypeHandlerRegistry typeHandlerRegistry = ms.getConfiguration().getTypeHandlerRegistry();
        // 遍历参数映射
        for (ParameterMapping parameterMapping : parameterMappings) {
            Object value;
            String propertyName = parameterMapping.getProperty();
            // 检查BoundSql是否有附加参数
            if (boundSql.hasAdditionalParameter(propertyName)) {
                value = boundSql.getAdditionalParameter(propertyName);
            } else if (parameterObject == null) {
                value = null;
            } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                value = parameterObject;
            } else {
                // 使用MetaObject获取参数的值
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                value = metaObject.getValue(propertyName);
            }
            // 更新CacheKey的值，添加参数的值
            cacheKey.update(value);
        }
        // 如果配置的环境不为空，则更新CacheKey的值，添加环境的ID
        if (configuration.getEnvironment() != null) {
            cacheKey.update(configuration.getEnvironment().getId());
        }
        // 返回创建的CacheKey对象
        return cacheKey;
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
        if (closed) {
            throw new RuntimeException("Executor was closed.");
        }
        clearLocalCache();
        return doUpdate(ms, parameter);
    }

    protected abstract int doUpdate(MappedStatement ms, Object parameter) throws SQLException;
    protected abstract <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql);

    @Override
    public Transaction getTransaction() {
        if (closed) {
            throw new RuntimeException("Executor was closed.");
        }
        return transaction;
    }

    @Override
    public void commit(boolean required) throws SQLException {
        if (closed) {
            throw new RuntimeException("Cannot commit, transaction is already closed");
        }
        clearLocalCache();
        if (required) {
            transaction.commit();
        }
    }

    @Override
    public void rollback(boolean required) throws SQLException {
        if (!closed) {
            try {
                clearLocalCache();
            } finally {
                if (required) {
                    transaction.rollback();
                }
            }
        }
    }

    @Override
    public void clearLocalCache() {
        if (!closed) {
            localCache.clear();
        }
    }

    @Override
    public void close(boolean forceRollback) {
        try {
            try {
                // 回滚事务
                rollback(forceRollback);
            } finally {
                // 关闭事务
                transaction.close();
            }
        } catch (SQLException e) {
            // 捕获SQL异常，并记录警告日志
            logger.warn("Unexpected exception on closing transaction.  Cause: " + e);
        } finally {
            // 确保事务被设置为null，并将closed标志设置为true
            transaction = null;
            localCache = null;
            closed = true;
        }
    }

    protected void closeStatement(Statement statement) {
        // 如果statement不为空，则尝试关闭它
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException ignore) {
                // 捕获SQL异常，并忽略
            }
        }
    }

    @Override
    public void setExecutorWrapper(Executor executor) {
        this.wrapper = wrapper;
    }

}
