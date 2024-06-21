package com.huanyu.mybatis.executor.result;

import com.huanyu.mybatis.session.ResultContext;

/**
 * ClassName: DefaultResultContext
 * Package: com.huanyu.mybatis.executor.resultset
 * Description: 默认结果上下文
 *
 * @Author: 寰宇
 * @Create: 2024/6/20 16:42
 * @Version: 1.0
 */
public class DefaultResultContext implements ResultContext {

    // 结果对象
    private Object resultObject;
    // 结果计数（表明这是第几个结果对象）
    private int resultCount;

    public DefaultResultContext() {
        this.resultObject = null;
        this.resultCount = 0;
    }

    @Override
    public Object getResultObject() {
        return resultObject;
    }

    @Override
    public int getResultCount() {
        return resultCount;
    }

    public void nextResultObject(Object resultObject) {
        resultCount++;
        this.resultObject = resultObject;
    }
}
