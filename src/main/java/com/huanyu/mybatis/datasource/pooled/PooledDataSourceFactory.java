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

    public PooledDataSourceFactory() {
        this.dataSource = new PooledDataSource();
    }
}
