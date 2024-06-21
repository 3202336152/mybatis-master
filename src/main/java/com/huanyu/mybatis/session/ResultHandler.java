package com.huanyu.mybatis.session;

/**
 * ClassName: ResultHandler
 * Package: com.huanyu.mybatis.session
 * Description: 结果处理器
 *
 * @Author: 寰宇
 * @Create: 2024/6/15 16:12
 * @Version: 1.0
 */
public interface ResultHandler {

    /**
     * 处理结果
     */
    void handleResult(ResultContext context);
}
