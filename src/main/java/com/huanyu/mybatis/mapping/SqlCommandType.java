package com.huanyu.mybatis.mapping;

/**
 * ClassName: SqlCommandType
 * Package: com.huanyu.mybatis.mapping
 * Description: SQL 指令类型
 *
 * @Author: 寰宇
 * @Create: 2024/6/12 14:32
 * @Version: 1.0
 */
public enum SqlCommandType {
    /**
     * 未知
     */
    UNKNOWN,
    /**
     * 插入
     */
    INSERT,
    /**
     * 更新
     */
    UPDATE,
    /**
     * 删除
     */
    DELETE,
    /**
     * 查找
     */
    SELECT;
}
