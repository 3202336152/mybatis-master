package com.huanyu.mybatis.type;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * ClassName: IntegerTypeHandler
 * Package: com.huanyu.mybatis.type
 * Description: Integer类型处理器
 *
 * @Author: 寰宇
 * @Create: 2024/6/19 14:51
 * @Version: 1.0
 */
public class IntegerTypeHandler extends BaseTypeHandler<Integer> {
    @Override
    protected void setNonNullParameter(PreparedStatement ps, int i, Integer parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter);
    }
}
