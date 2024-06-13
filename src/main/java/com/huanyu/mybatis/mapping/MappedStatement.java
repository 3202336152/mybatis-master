package com.huanyu.mybatis.mapping;

import com.huanyu.mybatis.session.Configuration;

/**
 * ClassName: MappedStatement
 * Package: com.huanyu.mybatis.mapping
 * Description: 映射语句类
 * 该对象完整的表述出一个下面节点的信息
 *
 *      <insert id="addUser" parameterType="User">
 *         INSERT INTO `user`
 *         (`name`,`email`,`age`,`sex`,`schoolName`)
 *         VALUES
 *         (#{name},#{email},#{age},#{sex},#{schoolName})
 *     </insert>
 *
 * @Author: 寰宇
 * @Create: 2024/6/12 14:38
 * @Version: 1.0
 */
public class MappedStatement {

    // Configuration对象
    private Configuration configuration;

    // 查询语句的完整包名加方法名，例如：com.huanyu.mybatis.dao.iUserDao
    private String id;

    // SQL命令类型（如SELECT、INSERT、UPDATE、DELETE）。
    private SqlCommandType sqlCommandType;

    // 绑定的SQL对象，包含了SQL语句及其参数信息。
    private BoundSql boundSql;

    MappedStatement() {
        // constructor disabled
    }

    /**
     * 建造者
     */
    public static class Builder {

        private MappedStatement mappedStatement = new MappedStatement();

        public Builder(Configuration configuration, String id, SqlCommandType sqlCommandType, BoundSql boundSql) {
            mappedStatement.configuration = configuration;
            mappedStatement.id = id;
            mappedStatement.sqlCommandType = sqlCommandType;
            mappedStatement.boundSql = boundSql;
        }

        public MappedStatement build() {
            assert mappedStatement.configuration != null;
            assert mappedStatement.id != null;
            return mappedStatement;
        }

    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public String getId() {
        return id;
    }

    public SqlCommandType getSqlCommandType() {
        return sqlCommandType;
    }

    public BoundSql getBoundSql() {
        return boundSql;
    }

}
