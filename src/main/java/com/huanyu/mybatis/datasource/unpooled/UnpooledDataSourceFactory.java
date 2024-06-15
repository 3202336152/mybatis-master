package com.huanyu.mybatis.datasource.unpooled;

import com.huanyu.mybatis.datasource.DataSourceFactory;

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

    protected Properties props;

    @Override
    public void setProperties(Properties props) {
        this.props = props;
    }

    @Override
    public DataSource getDataSource() {
        UnpooledDataSource unpooledDataSource = new UnpooledDataSource();
        unpooledDataSource.setDriver(props.getProperty("driver"));
        unpooledDataSource.setUrl(props.getProperty("url"));
        unpooledDataSource.setUsername(props.getProperty("username"));
        unpooledDataSource.setPassword(props.getProperty("password"));
        return unpooledDataSource;
    }
}
