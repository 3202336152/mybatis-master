package com.huanyu.mybatis.executor.result;

import com.huanyu.mybatis.reflection.factory.ObjectFactory;
import com.huanyu.mybatis.session.ResultContext;
import com.huanyu.mybatis.session.ResultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: DefaultResultHandler
 * Package: com.huanyu.mybatis.executor.resultset
 * Description: 默认结果处理器
 *
 * @Author: 寰宇
 * @Create: 2024/6/20 16:45
 * @Version: 1.0
 */
public class DefaultResultHandler implements ResultHandler {

    private final List<Object> list;

    public DefaultResultHandler() {
        this.list = new ArrayList<>();
    }
    /**
     * 通过 ObjectFactory 反射工具类，产生特定的 List
     */
    @SuppressWarnings("unchecked")
    public DefaultResultHandler(ObjectFactory objectFactory) {
        this.list = objectFactory.create(List.class);
    }

    @Override
    public void handleResult(ResultContext context) {
        list.add(context.getResultObject());
    }

    public List<Object> getResultList() {
        return list;
    }
}
