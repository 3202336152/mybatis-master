package com.huanyu.mybatis.type;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * ClassName: BooleanTypeHandler
 * Package: com.huanyu.mybatis.type
 * Description: Boolean类型处理器
 *
 * @Author: 寰宇
 * @Create: 2024/6/19 14:50
 * @Version: 1.0
 */
public class BooleanTypeHandler extends BaseTypeHandler<Boolean> {
    @Override
    protected void setNonNullParameter(PreparedStatement ps, int i, Boolean parameter, JdbcType jdbcType) throws SQLException {
        ps.setBoolean(i, parameter);
    }
}
