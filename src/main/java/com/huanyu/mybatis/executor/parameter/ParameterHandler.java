package com.huanyu.mybatis.executor.parameter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * ClassName: ParameterHandler
 * Package: com.huanyu.mybatis.executor.parameter
 * Description: 参数处理器
 *
 * @Author: 寰宇
 * @Create: 2024/6/19 14:54
 * @Version: 1.0
 */
public interface ParameterHandler {

    /**
     * 获取参数
     */
    Object getParameterObject();

    /**
     * 设置参数
     */
    void setParameters(PreparedStatement ps) throws SQLException;
}
