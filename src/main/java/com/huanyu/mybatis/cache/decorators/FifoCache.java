package com.huanyu.mybatis.cache.decorators;

import com.huanyu.mybatis.cache.Cache;

import java.util.Deque;
import java.util.LinkedList;

/**
 * ClassName: FifoCache
 * Package: com.huanyu.mybatis.cache.decorators
 * Description:FIFO (first in, first out) cache decorator
 *
 * @Author: 寰宇
 * @Create: 2024/6/29 15:27
 * @Version: 1.0
 */
public class FifoCache implements Cache {

    // 被装饰对象
    private final Cache delegate;
    // 按照写入顺序保存了缓存数据的键
    private Deque<Object> keyList;
    // 缓存空间的大小
    private int size;

    public FifoCache(Cache delegate) {
        this.delegate = delegate;
        this.keyList = new LinkedList<>();
        this.size = 1024;
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public int getSize() {
        return delegate.getSize();
    }

    public void setSize(int size) {
        this.size = size;
    }

    /**
     * 向缓存写入一条数据
     * @param key 数据的键
     * @param value 数据的值
     */
    @Override
    public void putObject(Object key, Object value) {
        cycleKeyList(key);
        delegate.putObject(key, value);
    }

    @Override
    public Object getObject(Object key) {
        return delegate.getObject(key);
    }

    @Override
    public Object removeObject(Object key) {
        return delegate.removeObject(key);
    }

    @Override
    public void clear() {
        delegate.clear();
        keyList.clear();
    }

    /**
     * 记录当前放入的数据的键，同时根据空间设置清除超出的数据
     * @param key 当前放入的数据的键
     */
    private void cycleKeyList(Object key) {
        keyList.addLast(key);
        if (keyList.size() > size) {
            Object oldestKey = keyList.removeFirst();
            delegate.removeObject(oldestKey);
        }
    }
}
