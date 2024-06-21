package com.huanyu.mybatis.session;

/**
 * ClassName: ResultContext
 * Package: com.huanyu.mybatis.session
 * Description: 结果上下文
 *
 * @Author: 寰宇
 * @Create: 2024/6/20 16:43
 * @Version: 1.0
 */
public interface ResultContext {

    /**
     * 获取结果
     */
    Object getResultObject();

    /**
     * 获取记录数
     */
    int getResultCount();
}
