package com.huanyu.mybatis.scripting.xmltags;


import com.huanyu.mybatis.reflection.MetaObject;
import com.huanyu.mybatis.session.Configuration;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.OgnlRuntime;
import ognl.PropertyAccessor;

import java.util.HashMap;
import java.util.Map;

/**
 * ClassName: DynamicContext
 * Package: com.huanyu.mybatis.scripting.defaults.xmltags
 * Description: 动态上下文
 * 这是DynamicSqlSource的辅助类，用来记录DynamicSqlSource解析出来的
 * SQL片段信息
 * 参数信息
 * @Author: 寰宇
 * @Create: 2024/6/17 21:48
 * @Version: 1.0
 */
public class DynamicContext {

    public static final String PARAMETER_OBJECT_KEY = "_parameter";
    public static final String DATABASE_ID_KEY = "_databaseId";

    static {
        // 定义属性->getter方法映射，ContextMap到ContextAccessor的映射，注册到ognl运行时
        // 参考http://commons.apache.org/proper/commons-ognl/developer-guide.html
        OgnlRuntime.setPropertyAccessor(ContextMap.class, new ContextAccessor());
        // 将传入的参数对象统一封装为ContextMap对象（继承了HashMap对象），
        // 然后Ognl运行时环境在动态计算sql语句时，
        // 会按照ContextAccessor中描述的Map接口的方式来访问和读取ContextMap对象，获取计算过程中需要的参数。
        // ContextMap对象内部可能封装了一个普通的POJO对象，也可以是直接传递的Map对象，当然从外部是看不出来的，因为都是使用Map的接口来读取数据。
    }

    // 上下文环境
    private final ContextMap bindings;
    // 用于拼装SQL语句片段
    private final StringBuilder sqlBuilder = new StringBuilder();
    // 解析时的唯一编号，防止解析混乱
    private int uniqueNumber = 0;

    // 在DynamicContext的构造函数中，根据传入的参数对象是否为Map类型，有两个不同构造ContextMap的方式。
    // 而ContextMap作为一个继承了HashMap的对象，作用就是用于统一参数的访问方式：用Map接口方法来访问数据。
    // 具体来说，当传入的参数对象不是Map类型时，Mybatis会将传入的POJO对象用MetaObject对象来封装，
    // 当动态计算sql过程需要获取数据时，用Map接口的get方法包装 MetaObject对象的取值过程。
    /**
     * DynamicContext的构造方法
     * @param configuration 配置信息
     * @param parameterObject 用户传入的查询参数对象
     */
    public DynamicContext(Configuration configuration, Object parameterObject) {
        // 绝大多数调用的地方parameterObject为null
        if (parameterObject != null && !(parameterObject instanceof Map)) {
            // 获得参数对象的元对象
            MetaObject metaObject = configuration.newMetaObject(parameterObject);
            // 放入上下文信息
            bindings = new ContextMap(metaObject);
        } else {
            // 上下文信息为空
            bindings = new ContextMap(null);
        }
        // 把参数对象放入上下文信息
        bindings.put(PARAMETER_OBJECT_KEY, parameterObject);
        // 把数据库id放入上下文信息
        bindings.put(DATABASE_ID_KEY, configuration.getDatabaseId());
    }

    public Map<String, Object> getBindings() {
        return bindings;
    }

    public void bind(String name, Object value) {
        bindings.put(name, value);
    }

    /**
     * 增加拼接串
     * @param sql
     */
    public void appendSql(String sql) {
        sqlBuilder.append(sql);
        sqlBuilder.append(" ");
    }

    /**
     * 获取拼接结果
     * @return
     */
    public String getSql() {
        return sqlBuilder.toString().trim();
    }

    public int getUniqueNumber() {
        return uniqueNumber++;
    }

    /**
     * HashMap的子类
     */
    static class ContextMap extends HashMap<String, Object> {
        private static final long serialVersionUID = 2977601501966151582L;

        // 这里是用户查询时传入的参数对象的包装
        private MetaObject parameterMetaObject;
        public ContextMap(MetaObject parameterMetaObject) {
            this.parameterMetaObject = parameterMetaObject;
        }

        /**
         * 根据键索引值。会尝试从HashMap中寻找，失败后会再尝试从parameterMetaObject中寻找
         * @param key 键
         * @return 值
         */
        @Override
        public Object get(Object key) {
            String strKey = (String) key;
            // 先去map里找
            if (super.containsKey(strKey)) {
                return super.get(strKey);
            }

            // 如果没找到，再用ognl表达式去取值
            // 如person[0].birthdate.year
            if (parameterMetaObject != null) {
                // issue #61 do not modify the context when reading
                return parameterMetaObject.getValue(strKey);
            }

            return null;
        }
    }

    // 上下文访问器，静态内部类,实现OGNL的PropertyAccessor
    static class ContextAccessor implements PropertyAccessor {

        @Override
        public Object getProperty(Map context, Object target, Object name)
                throws OgnlException {
            Map map = (Map) target;

            Object result = map.get(name);
            if (result != null) {
                return result;
            }

            Object parameterObject = map.get(PARAMETER_OBJECT_KEY);
            if (parameterObject instanceof Map) {
                return ((Map)parameterObject).get(name);
            }

            return null;
        }

        @Override
        public void setProperty(Map context, Object target, Object name, Object value)
                throws OgnlException {
            Map<Object, Object> map = (Map<Object, Object>) target;
            map.put(name, value);
        }

        @Override
        public String getSourceAccessor(OgnlContext arg0, Object arg1, Object arg2) {
            return null;
        }

        @Override
        public String getSourceSetter(OgnlContext arg0, Object arg1, Object arg2) {
            return null;
        }
    }

}
