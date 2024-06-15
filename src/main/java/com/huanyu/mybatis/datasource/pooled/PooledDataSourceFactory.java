package com.huanyu.mybatis.datasource.pooled;

import com.huanyu.mybatis.datasource.unpooled.UnpooledDataSourceFactory;

import javax.sql.DataSource;

/**
 * ClassName: PooledDataSourceFactory
 * Package: com.huanyu.mybatis.datasource.pooled
 * Description: 有连接池的数据源工厂
 *
 * @Author: 寰宇
 * @Create: 2024/6/14 12:34
 * @Version: 1.0
 */
public class PooledDataSourceFactory extends UnpooledDataSourceFactory {

    @Override
    public DataSource getDataSource() {
        PooledDataSource pooledDataSource = new PooledDataSource();
        pooledDataSource.setDriver(props.getProperty("driver"));
        pooledDataSource.setUrl(props.getProperty("url"));
        pooledDataSource.setUsername(props.getProperty("username"));
        pooledDataSource.setPassword(props.getProperty("password"));
        return pooledDataSource;
    }
}
