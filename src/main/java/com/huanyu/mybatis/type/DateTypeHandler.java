package com.huanyu.mybatis.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

/**
 * ClassName: DateTypeHandler
 * Package: com.huanyu.mybatis.type
 * Description: 日期类型处理器
 *
 * @Author: 寰宇
 * @Create: 2024/6/25 12:37
 * @Version: 1.0
 */
public class DateTypeHandler extends BaseTypeHandler<Date> {
    @Override
    protected void setNonNullParameter(PreparedStatement ps, int i, Date parameter, JdbcType jdbcType) throws SQLException {
        ps.setTimestamp(i, new java.sql.Timestamp(parameter.getTime()));
    }

    @Override
    public Date getResult(ResultSet rs, String columnName) throws SQLException {
        Timestamp sqlTimestamp = rs.getTimestamp(columnName);
        if (sqlTimestamp != null) {
            return new Date(sqlTimestamp.getTime());
        }
        return null;
    }
}
