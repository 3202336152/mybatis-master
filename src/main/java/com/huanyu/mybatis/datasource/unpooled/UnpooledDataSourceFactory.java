package com.huanyu.mybatis.datasource.unpooled;

import com.huanyu.mybatis.datasource.DataSourceFactory;
import com.huanyu.mybatis.reflection.MetaObject;
import com.huanyu.mybatis.reflection.SystemMetaObject;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * ClassName: UnpooledDataSourceFactory
 * Package: com.huanyu.mybatis.datasource.unpooled
 * Description: 无池化数据源工厂
 *
 * @Author: 寰宇
 * @Create: 2024/6/14 12:35
 * @Version: 1.0
 */
public class UnpooledDataSourceFactory implements DataSourceFactory {

    protected DataSource dataSource;

    public UnpooledDataSourceFactory() {
        this.dataSource = new UnpooledDataSource();
    }

    @Override
    public void setProperties(Properties props) {
        // 将 dataSource 封装为 MetaObject，以便使用反射操作其属性
        MetaObject metaObject = SystemMetaObject.forObject(dataSource);
        // 遍历传入的属性集
        for (Object key : props.keySet()) {
            // 获取属性名称
            String propertyName = (String) key;
            // 检查 metaObject 是否有该属性的 setter 方法
            if (metaObject.hasSetter(propertyName)) {
                // 获取属性值
                String value = (String) props.get(propertyName);
                // 转换属性值为合适的类型
                Object convertedValue = convertValue(metaObject, propertyName, value);
                // 设置属性值
                metaObject.setValue(propertyName, convertedValue);
            }
        }
    }

    /**
     * 根据setter的类型,将配置文件中的值强转成相应的类型
     */
    private Object convertValue(MetaObject metaObject, String propertyName, String value) {
        Object convertedValue = value;
        Class<?> targetType = metaObject.getSetterType(propertyName);
        if (targetType == Integer.class || targetType == int.class) {
            convertedValue = Integer.valueOf(value);
        } else if (targetType == Long.class || targetType == long.class) {
            convertedValue = Long.valueOf(value);
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            convertedValue = Boolean.valueOf(value);
        }
        return convertedValue;
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }
}
