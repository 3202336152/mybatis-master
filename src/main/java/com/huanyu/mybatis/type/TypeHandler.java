package com.huanyu.mybatis.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * ClassName: TypeHandler
 * Package: com.huanyu.mybatis.type
 * Description: 类型处理器
 *
 * @Author: 寰宇
 * @Create: 2024/6/17 21:23
 * @Version: 1.0
 */
public interface TypeHandler<T> {

    /**
     * 设置参数
     */
    void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException;

    /**
     * 获取结果
     */
    T getResult(ResultSet rs, String columnName) throws SQLException;

}
