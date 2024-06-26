package com.huanyu.mybatis.mapping;

import com.huanyu.mybatis.cache.Cache;
import com.huanyu.mybatis.scripting.LanguageDriver;
import com.huanyu.mybatis.session.Configuration;

import java.util.Collections;
import java.util.List;

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

    // Mapper文件的磁盘路径
    private String resource;

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

    private List<ResultMap> resultMaps;

    // 执行该语句前是否清除一二级缓存
    private boolean flushCacheRequired;
    // 存储了主键的属性名
    private String[] keyProperties;
    private String[] keyColumns;

    private Cache cache;
    private boolean useCache;

    MappedStatement() {
        // constructor disabled
    }

    public BoundSql getBoundSql(Object parameterObject) {
        // 调用 SqlSource#getBoundSql
        return sqlSource.getBoundSql(parameterObject);
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
            mappedStatement.resultMaps = Collections.unmodifiableList(mappedStatement.resultMaps);
            return mappedStatement;
        }

        public Builder resource(String resource) {
            mappedStatement.resource = resource;
            return this;
        }

        public String id() {
            return mappedStatement.id;
        }

        public Builder resultMaps(List<ResultMap> resultMaps) {
            mappedStatement.resultMaps = resultMaps;
            return this;
        }

        public Builder cache(Cache cache) {
            mappedStatement.cache = cache;
            return this;
        }

        public Builder flushCacheRequired(boolean flushCacheRequired) {
            mappedStatement.flushCacheRequired = flushCacheRequired;
            return this;
        }

        public Builder useCache(boolean useCache) {
            mappedStatement.useCache = useCache;
            return this;
        }

    }

    private static String[] delimitedStringToArray(String in) {
        if (in == null || in.trim().length() == 0) {
            return null;
        } else {
            return in.split(",");
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

    public List<ResultMap> getResultMaps() {
        return resultMaps;
    }

    public String[] getKeyColumns() {
        return keyColumns;
    }

    public String[] getKeyProperties() {
        return keyProperties;
    }


    public String getResource() {
        return resource;
    }

    public boolean isFlushCacheRequired() {
        return flushCacheRequired;
    }

    public boolean isUseCache() {
        return useCache;
    }

    public Cache getCache() {
        return cache;
    }

}
