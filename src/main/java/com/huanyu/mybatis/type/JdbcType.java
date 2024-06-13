package com.huanyu.mybatis.type;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * ClassName: JdbcType
 * Package: com.huanyu.mybatis.type
 * Description: JDBC类型枚举
 *
 * @Author: 寰宇
 * @Create: 2024/6/13 16:12
 * @Version: 1.0
 */
public enum JdbcType {
    INTEGER(Types.INTEGER),
    FLOAT(Types.FLOAT),
    DOUBLE(Types.DOUBLE),
    DECIMAL(Types.DECIMAL),
    VARCHAR(Types.VARCHAR),
    TIMESTAMP(Types.TIMESTAMP);

    public final int TYPE_CODE;
    private static Map<Integer,JdbcType> codeLookup = new HashMap<>();

    // 就将数字对应的枚举型放入 HashMap
    static {
        for (JdbcType type : JdbcType.values()) {
            codeLookup.put(type.TYPE_CODE, type);
        }
    }

    JdbcType(int code) {
        this.TYPE_CODE = code;
    }

    public static JdbcType forCode(int code)  {
        return codeLookup.get(code);
    }

}
