package com.huanyu.mybatis.type;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * ClassName: DoubleTypeHandler
 * Package: com.huanyu.mybatis.type
 * Description: Double类型处理器
 *
 * @Author: 寰宇
 * @Create: 2024/6/19 14:49
 * @Version: 1.0
 */
public class DoubleTypeHandler extends BaseTypeHandler<Double> {
    @Override
    protected void setNonNullParameter(PreparedStatement ps, int i, Double parameter, JdbcType jdbcType) throws SQLException {
        ps.setDouble(i, parameter);
    }
}
