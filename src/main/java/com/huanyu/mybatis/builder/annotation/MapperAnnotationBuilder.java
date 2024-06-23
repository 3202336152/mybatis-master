package com.huanyu.mybatis.builder.annotation;

import com.huanyu.mybatis.annotations.Delete;
import com.huanyu.mybatis.annotations.Insert;
import com.huanyu.mybatis.annotations.Select;
import com.huanyu.mybatis.annotations.Update;
import com.huanyu.mybatis.binding.MapperMethod;
import com.huanyu.mybatis.builder.MapperBuilderAssistant;
import com.huanyu.mybatis.mapping.SqlCommandType;
import com.huanyu.mybatis.mapping.SqlSource;
import com.huanyu.mybatis.scripting.LanguageDriver;
import com.huanyu.mybatis.session.Configuration;
import com.huanyu.mybatis.session.ResultHandler;
import com.huanyu.mybatis.session.RowBounds;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * ClassName: MapperAnnotationBuilder
 * Package: com.huanyu.mybatis.builder.annotation
 * Description: 注解配置构建器 Mapper
 *
 * @Author: 寰宇
 * @Create: 2024/6/23 15:32
 * @Version: 1.0
 */
public class MapperAnnotationBuilder {

    // 保存SQL注解类型的集合
    private final Set<Class<? extends Annotation>> sqlAnnotationTypes = new HashSet<>();

    // 配置对象
    private Configuration configuration;
    // 辅助类对象
    private MapperBuilderAssistant assistant;
    // 注解所在的类
    private Class<?> type;

    // 构造函数，初始化配置对象和辅助类对象
    public MapperAnnotationBuilder(Configuration configuration, Class<?> type) {
        // 把“.”换成“/”就从类名得到了Mapper的路径，当然，只是猜测。因为这是规范
        String resource = type.getName().replace(".", "/") + ".java (best guess)";
        this.assistant = new MapperBuilderAssistant(configuration, resource);
        this.configuration = configuration;
        this.type = type;

        // 添加SQL注解类型
        sqlAnnotationTypes.add(Select.class);
        sqlAnnotationTypes.add(Insert.class);
        sqlAnnotationTypes.add(Update.class);
        sqlAnnotationTypes.add(Delete.class);
    }

    /**
     * 解析包含注解的接口文档
     */
    public void parse() {
        String resource = type.toString();
        // 如果资源未加载，则进行加载
        if (!configuration.isResourceLoaded(resource)) {
            // 设置命名空间
            assistant.setCurrentNamespace(type.getName());
            // 获取IUserDao接口的全部方法
            Method[] methods = type.getMethods();
            for (Method method : methods) {
                // 排除桥接方法
                // JDK 1.5 引入泛型后，为了使Java的泛型方法生成的字节码和 1.5 版本前的字节码相兼容，由编译器自动生成的方法，这个就是桥接方法。
                // 就是说一个子类在继承（或实现）一个父类（或接口）的泛型方法时，在子类中明确指定了泛型类型，那么在编译时编译器会自动生成桥接方法
                if (!method.isBridge()) {
                    // 解析方法中的SQL语句
                    parseStatement(method);
                }
            }
        }
    }

    // 解析方法中的SQL语句
    private void parseStatement(Method method) {
        // 获取方法参数类型
        Class<?> parameterTypeClass = getParameterType(method);
        // 获取语言驱动器
        LanguageDriver languageDriver = getLanguageDriver(method);
        // 从注解中获取SQL源
        SqlSource sqlSource = getSqlSourceFromAnnotations(method, parameterTypeClass, languageDriver);

        if (sqlSource != null) {
            // 构建MappedStatement的ID
            final String mappedStatementId = type.getName() + "." + method.getName();
            // 获取SQL命令类型
            SqlCommandType sqlCommandType = getSqlCommandType(method);
            boolean isSelect = sqlCommandType == SqlCommandType.SELECT;

            String resultMapId = null;
            if (isSelect) {
                // 解析结果映射
                resultMapId = parseResultMap(method);
            }

            // 添加MappedStatement
            assistant.addMappedStatement(
                    mappedStatementId,
                    sqlSource,
                    sqlCommandType,
                    parameterTypeClass,
                    resultMapId,
                    getReturnType(method),
                    languageDriver
            );
        }
    }

    // 获取方法的返回类型
    private Class<?> getReturnType(Method method) {
        // 获取方法的返回类型
        Class<?> returnType = method.getReturnType();

        // 如果返回类型是Collection的子类
        if (Collection.class.isAssignableFrom(returnType)) {
            // 获取方法的泛型返回类型
            Type returnTypeParameter = method.getGenericReturnType();

            // 如果返回类型是参数化类型
            if (returnTypeParameter instanceof ParameterizedType) {
                // 获取实际的泛型参数类型
                Type[] actualTypeArguments = ((ParameterizedType) returnTypeParameter).getActualTypeArguments();

                // 如果泛型参数类型不为空且长度为1
                if (actualTypeArguments != null && actualTypeArguments.length == 1) {
                    returnTypeParameter = actualTypeArguments[0];

                    // 如果泛型参数类型是Class类型
                    if (returnTypeParameter instanceof Class) {
                        returnType = (Class<?>) returnTypeParameter;
                    }
                    // 如果泛型参数类型是参数化类型
                    else if (returnTypeParameter instanceof ParameterizedType) {
                        returnType = (Class<?>) ((ParameterizedType) returnTypeParameter).getRawType();
                    }
                    // 如果泛型参数类型是泛型数组类型
                    else if (returnTypeParameter instanceof GenericArrayType) {
                        Class<?> componentType = (Class<?>) ((GenericArrayType) returnTypeParameter).getGenericComponentType();
                        returnType = Array.newInstance(componentType, 0).getClass();
                    }
                }
            }
        }

        // 返回最终确定的返回类型
        return returnType;
    }

    // 解析结果映射
    private String parseResultMap(Method method) {
        // 生成结果映射名称的后缀
        StringBuilder suffix = new StringBuilder();

        // 遍历方法的参数类型并添加到后缀
        for (Class<?> c : method.getParameterTypes()) {
            suffix.append("-");
            suffix.append(c.getSimpleName());
        }

        // 如果没有参数类型则添加-void
        if (suffix.length() < 1) {
            suffix.append("-void");
        }

        // 生成结果映射ID
        String resultMapId = type.getName() + "." + method.getName() + suffix;

        // 获取方法的返回类型
        Class<?> returnType = getReturnType(method);

        // 添加结果映射到助手中
        assistant.addResultMap(resultMapId, returnType, new ArrayList<>());

        // 返回结果映射ID
        return resultMapId;
    }

    // 获取SQL命令类型
    private SqlCommandType getSqlCommandType(Method method) {
        // 获取方法的SQL注解类型
        Class<? extends Annotation> type = getSqlAnnotationType(method);

        // 如果没有找到对应的注解类型，则返回UNKNOWN
        if (type == null) {
            return SqlCommandType.UNKNOWN;
        }

        // 返回对应的SQL命令类型
        return SqlCommandType.valueOf(type.getSimpleName().toUpperCase(Locale.ENGLISH));
    }


    // 从注解中获取SQL源
    private SqlSource getSqlSourceFromAnnotations(Method method, Class<?> parameterType, LanguageDriver languageDriver) {
        try {
            // 获取方法上的SQL注解类型
            Class<? extends Annotation> sqlAnnotationType = getSqlAnnotationType(method);
            // 如果方法上存在SQL注解
            if (sqlAnnotationType != null) {
                // 获取SQL注解实例
                Annotation sqlAnnotation = method.getAnnotation(sqlAnnotationType);
                // 通过反射调用注解的value方法获取SQL语句字符串数组
                final String[] strings = (String[]) sqlAnnotation.getClass().getMethod("value").invoke(sqlAnnotation);
                // 从字符串数组构建SQL源
                return buildSqlSourceFromStrings(strings, parameterType, languageDriver);
            }
            return null;
        } catch (Exception e) {
            // 捕获异常并抛出运行时异常
            throw new RuntimeException("无法在SQL注解上找到value方法. 原因: " + e);
        }
    }

    // 从字符串数组构建SQL源
    private SqlSource buildSqlSourceFromStrings(String[] strings, Class<?> parameterTypeClass, LanguageDriver languageDriver) {
        // 创建一个StringBuilder对象用于拼接SQL语句
        final StringBuilder sql = new StringBuilder();

        // 遍历字符串数组并拼接成一个完整的SQL语句
        for (String fragment : strings) {
            sql.append(fragment);
            sql.append(" ");
        }

        // 使用语言驱动器创建SQL源
        return languageDriver.createSqlSource(configuration, sql.toString(), parameterTypeClass);
    }

    // 获取方法的SQL注解类型
    private Class<? extends Annotation> getSqlAnnotationType(Method method) {
        // 遍历SQL注解类型数组
        for (Class<? extends Annotation> type : sqlAnnotationTypes) {
            // 获取方法上的注解
            Annotation annotation = method.getAnnotation(type);
            // 如果方法上存在该类型的注解，则返回该注解类型
            if (annotation != null) return type;
        }
        // 如果方法上不存在任何SQL注解类型，则返回null
        return null;
    }

    // 获取语言驱动器
    private LanguageDriver getLanguageDriver(Method method) {
        // 获取默认的语言驱动器类
        Class<?> langClass = configuration.getLanguageRegistry().getDefaultDriverClass();
        // 从配置中获取对应的语言驱动器实例
        return configuration.getLanguageRegistry().getDriver(langClass);
    }

    // 获取方法的参数类型
    private Class<?> getParameterType(Method method) {
        Class<?> parameterType = null;
        // 获取方法的所有参数类型
        Class<?>[] parameterTypes = method.getParameterTypes();

        // 遍历参数类型数组
        for (Class<?> clazz : parameterTypes) {
            // 过滤掉RowBounds和ResultHandler类型的参数
            if (!RowBounds.class.isAssignableFrom(clazz) && !ResultHandler.class.isAssignableFrom(clazz)) {
                // 如果parameterType为空，则赋值当前参数类型
                if (parameterType == null) {
                    parameterType = clazz;
                }
                // 如果parameterType不为空，则设置为ParamMap类型
                else {
                    parameterType = MapperMethod.ParamMap.class;
                }
            }
        }
        // 返回最终确定的参数类型
        return parameterType;
    }

}