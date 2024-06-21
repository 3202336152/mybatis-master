package com.huanyu.mybatis.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * ClassName: StringTypeHandler
 * Package: com.huanyu.mybatis.type
 * Description: String类型处理器
 *
 * @Author: 寰宇
 * @Create: 2024/6/19 14:48
 * @Version: 1.0
 */
public class StringTypeHandler extends BaseTypeHandler<String> {
    @Override
    protected void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter);
    }

    @Override
    public String getResult(ResultSet rs, String columnName) throws SQLException {
        return rs.getString(columnName);
    }
}
