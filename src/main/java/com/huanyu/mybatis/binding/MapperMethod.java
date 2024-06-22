package com.huanyu.mybatis.binding;

import com.huanyu.mybatis.mapping.MappedStatement;
import com.huanyu.mybatis.mapping.SqlCommandType;
import com.huanyu.mybatis.session.Configuration;
import com.huanyu.mybatis.session.SqlSession;

import java.lang.reflect.Method;
import java.util.*;

/**
 * ClassName: MapperMethod
 * Package: com.huanyu.mybatis.binding
 * Description: 映射器方法
 *
 * @Author: 寰宇
 * @Create: 2024/6/12 14:31
 * @Version: 1.0
 */
public class MapperMethod {

    // 记录了sql的名称和类型
    private final SqlCommand command;

    private final MethodSignature method;

    /**
     * MapperMethod的构造方法
     *
     * @param mapperInterface 映射接口
     * @param method          映射接口中的具体方法
     * @param configuration   配置信息Configuration
     */
    public MapperMethod(Class<?> mapperInterface, Method method, Configuration configuration) {
        this.method = new MethodSignature(configuration, method);
        this.command = new SqlCommand(configuration, mapperInterface, method);
    }

    /**
     * 执行映射接口中的方法
     * @param sqlSession sqlSession接口的实例，通过它可以进行数据库的操作
     * @param args 执行接口方法时传入的参数
     * @return 数据库操作结果
     */
    public Object execute(SqlSession sqlSession, Object[] args) {
        Object result = null;
        // 根据SQL语句类型，执行不同操作
        switch (command.getType()) {
            case INSERT: {
                Object param = method.convertArgsToSqlCommandParam(args);
                result = sqlSession.insert(command.getName(), param);
                break;
            }
            case DELETE: {
                Object param = method.convertArgsToSqlCommandParam(args);
                result = sqlSession.delete(command.getName(), param);
                break;
            }
            case UPDATE: {
                Object param = method.convertArgsToSqlCommandParam(args);
                result = sqlSession.update(command.getName(), param);
                break;
            }
            case SELECT: {
                Object param = method.convertArgsToSqlCommandParam(args);
                if (method.returnsMany) {
                    result = sqlSession.selectList(command.getName(), param);
                } else {
                    result = sqlSession.selectOne(command.getName(), param);
                }
                break;
            }
            default:
                throw new RuntimeException("Unknown execution method for: " + command.getName());
        }
        return result;
    }

    /**
     * SQL 指令
     */
    public static class SqlCommand {

        // SQL语句的名称
        private final String name;

        // SQL语句的种类，一共分为以下六种：增、删、改、查、清缓存、未知
        private final SqlCommandType type;

        public SqlCommand(Configuration configuration, Class<?> mapperInterface, Method method) {
            String statementName = mapperInterface.getName() + "." + method.getName();
            MappedStatement ms = configuration.getMappedStatement(statementName);
            name = ms.getId();
            type = ms.getSqlCommandType();
        }

        public String getName() {
            return name;
        }

        public SqlCommandType getType() {
            return type;
        }
    }

    /**
     * 方法签名
     */
    public static class MethodSignature {

        private final boolean returnsMany;
        private final Class<?> returnType;
        private final SortedMap<Integer, String> params;

        public MethodSignature(Configuration configuration,Method method) {
            this.returnType = method.getReturnType();
            this.returnsMany = (configuration.getObjectFactory().isCollection(this.returnType) || this.returnType.isArray());
            this.params = Collections.unmodifiableSortedMap(getParams(method));
        }

        public Object convertArgsToSqlCommandParam(Object[] args) {
            final int paramCount = params.size();
            if (args == null || paramCount == 0) {
                //如果没参数
                return null;
            } else if (paramCount == 1) {
                return args[params.keySet().iterator().next().intValue()];
            } else {
                // 否则，返回一个ParamMap，修改参数名，参数名就是其位置
                final Map<String, Object> param = new ParamMap<Object>();
                int i = 0;
                for (Map.Entry<Integer, String> entry : params.entrySet()) {
                    // 1.先加一个#{0},#{1},#{2}...参数
                    param.put(entry.getValue(), args[entry.getKey().intValue()]);
                    // issue #71, add param names as param1, param2...but ensure backward compatibility
                    final String genericParamName = "param" + (i + 1);
                    if (!param.containsKey(genericParamName)) {
                        /*
                         * 2.再加一个#{param1},#{param2}...参数
                         * 你可以传递多个参数给一个映射器方法。如果你这样做了,
                         * 默认情况下它们将会以它们在参数列表中的位置来命名,比如:#{param1},#{param2}等。
                         * 如果你想改变参数的名称(只在多参数情况下) ,那么你可以在参数上使用@Param(“paramName”)注解。
                         */
                        param.put(genericParamName, args[entry.getKey()]);
                    }
                    i++;
                }
                return param;
            }
        }

        private SortedMap<Integer, String> getParams(Method method) {
            // 用一个TreeMap，这样就保证还是按参数的先后顺序
            final SortedMap<Integer, String> params = new TreeMap<Integer, String>();
            final Class<?>[] argTypes = method.getParameterTypes();
            for (int i = 0; i < argTypes.length; i++) {
                String paramName = String.valueOf(params.size());
                // 不做 Param 的实现，这部分不处理。如果扩展学习，需要添加 Param 注解并做扩展实现。
                params.put(i, paramName);
            }
            return params;
        }

    }

    /**
     * 参数map，静态内部类,更严格的get方法，如果没有相应的key，报错
     */
    public static class ParamMap<V> extends HashMap<String, V> {

        private static final long serialVersionUID = -2212268410512043556L;

        @Override
        public V get(Object key) {
            if (!super.containsKey(key)) {
                throw new RuntimeException("Parameter '" + key + "' not found. Available parameters are " + keySet());
            }
            return super.get(key);
        }

    }
}
