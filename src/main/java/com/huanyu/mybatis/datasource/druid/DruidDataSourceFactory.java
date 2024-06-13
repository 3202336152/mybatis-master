package com.huanyu.mybatis.datasource.druid;

import com.alibaba.druid.pool.DruidDataSource;
import com.huanyu.mybatis.datasource.DataSourceFactory;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * ClassName: DruidDataSourceFactory
 * Package: com.huanyu.mybatis.datasource.druid
 * Description: Druid 数据源工厂
 *
 * @Author: 寰宇
 * @Create: 2024/6/13 16:00
 * @Version: 1.0
 */
public class DruidDataSourceFactory implements DataSourceFactory {

    private Properties props;
    @Override
    public void setProperties(Properties props) {
        this.props = props;
    }

    @Override
    public DataSource getDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(props.getProperty("driver"));
        dataSource.setUrl(props.getProperty("url"));
        dataSource.setUsername(props.getProperty("username"));
        dataSource.setPassword(props.getProperty("password"));
        return dataSource;
    }
}
