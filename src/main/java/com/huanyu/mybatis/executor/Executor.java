package com.huanyu.mybatis.executor;

import com.huanyu.mybatis.cache.CacheKey;
import com.huanyu.mybatis.mapping.BoundSql;
import com.huanyu.mybatis.mapping.MappedStatement;
import com.huanyu.mybatis.session.ResultHandler;
import com.huanyu.mybatis.session.RowBounds;
import com.huanyu.mybatis.transaction.Transaction;

import java.sql.SQLException;
import java.util.List;

/**
 * ClassName: Executor
 * Package: com.huanyu.mybatis.executor
 * Description: 执行器
 *
 * @Author: 寰宇
 * @Create: 2024/6/15 16:11
 * @Version: 1.0
 */
public interface Executor {

    ResultHandler NO_RESULT_HANDLER = null;

    // 数据更新操作，其中数据的增加、删除、更新均可由该方法实现
    int update(MappedStatement ms, Object parameter) throws SQLException;

    // 数据查询操作，返回结果为列表形式
    <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException;

    /**
     * 执行查询操作,返回结果为列表形式
     * @param ms 映射语句对象
     * @param parameter 参数对象
     * @param resultHandler 结果处理器
     * @param <E> 输出结果类型
     * @return 查询结果
     * @throws SQLException
     */
    <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException;

    // 获取事务
    Transaction getTransaction();
    // 提交事务
    void commit(boolean required) throws SQLException;
    // 回滚事务
    void rollback(boolean required) throws SQLException;
    // 关闭执行器
    void close(boolean forceRollback);

    // 清理Session缓存
    void clearLocalCache();

    // 创建缓存 Key
    CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql);

    void setExecutorWrapper(Executor executor);
}
