package com.huanyu.mybatis.datasource;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * ClassName: DataSourceFactory
 * Package: com.huanyu.mybatis.datasource
 * Description: 数据源工厂
 *
 * @Author: 寰宇
 * @Create: 2024/6/13 15:59
 * @Version: 1.0
 */
public interface DataSourceFactory {

    /**
     * 设置工厂属性
     * @param props 属性
     */
    void setProperties(Properties props);

    /**
     * 从工厂中获取产品
     * @return DataSource对象
     */
    DataSource getDataSource();
}
