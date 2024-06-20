package com.huanyu.mybatis.type;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * ClassName: FloatTypeHandler
 * Package: com.huanyu.mybatis.type
 * Description: Float类型处理器
 *
 * @Author: 寰宇
 * @Create: 2024/6/19 14:51
 * @Version: 1.0
 */
public class FloatTypeHandler extends BaseTypeHandler<Float> {
    @Override
    protected void setNonNullParameter(PreparedStatement ps, int i, Float parameter, JdbcType jdbcType) throws SQLException {
        ps.setFloat(i, parameter);
    }
}
