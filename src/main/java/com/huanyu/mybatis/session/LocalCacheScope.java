package com.huanyu.mybatis.session;

/**
 * ClassName: LocalCacheScope
 * Package: com.huanyu.mybatis.session
 * Description: 本地缓存机制；
 * SESSION 默认值，缓存一个会话中执行的所有查询
 * STATEMENT 本地会话仅用在语句执行上，对相同 SqlSession 的不同调用将不做数据共享
 * @Author: 寰宇
 * @Create: 2024/6/28 18:34
 * @Version: 1.0
 */
public enum LocalCacheScope {
    SESSION,
    STATEMENT
}
