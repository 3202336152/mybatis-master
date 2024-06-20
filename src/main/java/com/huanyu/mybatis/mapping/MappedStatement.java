package com.huanyu.mybatis.mapping;

import com.huanyu.mybatis.scripting.LanguageDriver;
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
    private SqlSource sqlSource;

    Class<?> resultType;

    private LanguageDriver lang;

    MappedStatement() {
        // constructor disabled
    }

    /**
     * 建造者
     */
    public static class Builder {

        private MappedStatement mappedStatement = new MappedStatement();

        public Builder(Configuration configuration, String id, SqlCommandType sqlCommandType, SqlSource sqlSource, Class<?> resultType) {
            mappedStatement.configuration = configuration;
            mappedStatement.id = id;
            mappedStatement.sqlCommandType = sqlCommandType;
            mappedStatement.sqlSource = sqlSource;
            mappedStatement.resultType = resultType;
            mappedStatement.lang = configuration.getDefaultScriptingLanguageInstance();
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

    public SqlSource getSqlSource() {
        return sqlSource;
    }

    public Class<?> getResultType() {
        return resultType;
    }

    public LanguageDriver getLang() {
        return lang;
    }

}
