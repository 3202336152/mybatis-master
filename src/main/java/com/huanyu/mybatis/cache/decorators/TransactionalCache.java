package com.huanyu.mybatis.cache.decorators;

import com.huanyu.mybatis.cache.Cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * ClassName: TransactionalCache
 * Package: com.huanyu.mybatis.cache.decorators
 * Description: 二级缓存事务缓冲区
 * 此类保存会话期间要添加到二级缓存的所有缓存条目。
 * 调用提交时，条目将发送到缓存；如果会话回滚，条目将被丢弃。
 * 已添加阻塞缓存支持。因此，任何返回缓存未命中的 get()
 * 后面都会跟着 put()，因此可以释放与该键关联的任何锁。
 * @Author: 寰宇
 * @Create: 2024/6/29 15:29
 * @Version: 1.0
 */
public class TransactionalCache implements Cache {

    // 被装饰的对象
    private Cache delegate;
    // 事务提交后是否直接清理缓存，确保数据一致性。
    private boolean clearOnCommit;
    // 事务提交时需要写入缓存的数据，防止重复查询。
    private Map<Object, Object> entriesToAddOnCommit;
    // 缓存查询未命中的数据
    private Set<Object> entriesMissedInCache;

    public TransactionalCache(Cache delegate) {
        // delegate = FifoCache
        this.delegate = delegate;
        // 默认 commit 时不清缓存
        this.clearOnCommit = false;
        this.entriesToAddOnCommit = new HashMap<>();
        this.entriesMissedInCache = new HashSet<>();
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public int getSize() {
        return delegate.getSize();
    }

    /**
     * 从缓存中读取一条信息
     * @param key 信息的键
     * @return 信息的值
     */
    @Override
    public Object getObject(Object key) {
        // key：CacheKey 拼装后的哈希码
        // 从缓存中读取对应的数据
        Object object = delegate.getObject(key);
        if (object == null) {
            // 记录该缓存未命中
            entriesMissedInCache.add(key);
        }
        // 如果设置了提交时立马清除，则直接返回null，否则返回查询的结果
        return clearOnCommit ? null : object;
    }

    /**
     * 向缓存写入一条信息
     * @param key 信息的键
     * @param object 信息的值
     */
    @Override
    public void putObject(Object key, Object object) {
        // 先放入到entriesToAddOnCommit列表中暂存
        entriesToAddOnCommit.put(key, object);
    }

    @Override
    public Object removeObject(Object key) {
        return null;
    }

    @Override
    public void clear() {
        clearOnCommit = true;
        entriesToAddOnCommit.clear();
    }

    /**
     * 提交事务
     */
    public void commit() {
        // 如果设置了事务提交后清理缓存
        if (clearOnCommit) {
            // 清理缓存
            delegate.clear();
        }
        // 将为写入缓存的操作写入缓存
        flushPendingEntries();
        // 清理环境
        reset();
    }

    /**
     * 回滚事务
     */
    public void rollback() {
        // 删除缓存未命中的数据
        unlockMissedEntries();
        reset();
    }

    /**
     * 清理环境
     */
    private void reset() {
        clearOnCommit = false;
        entriesToAddOnCommit.clear();
        entriesMissedInCache.clear();
    }

    /**
     * 刷新数据到 MappedStatement#Cache 中，也就是把数据填充到 Mapper XML 级别下。
     * flushPendingEntries 方法把事务缓存下的数据，填充到 FifoCache 中。
     * 将未写入缓存的数据写入缓存
     */
    private void flushPendingEntries() {
        // 将entriesToAddOnCommit中的数据写入缓存
        for (Map.Entry<Object, Object> entry : entriesToAddOnCommit.entrySet()) {
            delegate.putObject(entry.getKey(), entry.getValue());
        }
        // 将entriesMissedInCache中的数据写入缓存
        for (Object entry : entriesMissedInCache) {
            if (!entriesToAddOnCommit.containsKey(entry)) {
                delegate.putObject(entry, null);
            }
        }
    }

    /**
     * 删除缓存未命中的数据
     */
    private void unlockMissedEntries() {
        for (Object entry : entriesMissedInCache) {
            delegate.putObject(entry, null);
        }
    }
}
