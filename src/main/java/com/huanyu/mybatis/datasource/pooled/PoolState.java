package com.huanyu.mybatis.datasource.pooled;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: PoolState
 * Package: com.huanyu.mybatis.datasource.pooled
 * Description: 池状态
 *
 * @Author: 寰宇
 * @Create: 2024/6/14 12:33
 * @Version: 1.0
 */
public class PoolState {

    // 池化数据源
    protected PooledDataSource dataSource;

    // 空闲连接
    protected final List<PooledConnection> idleConnections = new ArrayList<>();
    // 活跃连接
    protected final List<PooledConnection> activeConnections = new ArrayList<>();

    // 请求次数
    protected long requestCount = 0;

    // 取出请求花费时间的累计值。从准备取出请求到取出结束的时间为取出请求花费的时间
    protected long accumulatedRequestTime = 0;
    // 累积被检出的时间
    protected long accumulatedCheckoutTime = 0;
    // 声明的过期连接数
    protected long claimedOverdueConnectionCount = 0;
    // 过期的连接数的总检出时长
    protected long accumulatedCheckoutTimeOfOverdueConnections = 0;

    // 总等待时间
    protected long accumulatedWaitTime = 0;
    // 要等待的次数
    protected long hadToWaitCount = 0;
    // 失败连接次数
    protected long badConnectionCount = 0;

    public PoolState(PooledDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public synchronized long getRequestCount() {
        return requestCount;
    }

    public synchronized long getAverageRequestTime() {
        return requestCount == 0 ? 0 : accumulatedRequestTime / requestCount;
    }

    public synchronized long getAverageWaitTime() {
        return hadToWaitCount == 0 ? 0 : accumulatedWaitTime / hadToWaitCount;
    }

    public synchronized long getHadToWaitCount() {
        return hadToWaitCount;
    }

    public synchronized long getBadConnectionCount() {
        return badConnectionCount;
    }

    public synchronized long getClaimedOverdueConnectionCount() {
        return claimedOverdueConnectionCount;
    }

    public synchronized long getAverageOverdueCheckoutTime() {
        return claimedOverdueConnectionCount == 0 ? 0 : accumulatedCheckoutTimeOfOverdueConnections / claimedOverdueConnectionCount;
    }

    public synchronized long getAverageCheckoutTime() {
        return requestCount == 0 ? 0 : accumulatedCheckoutTime / requestCount;
    }

    public synchronized int getIdleConnectionCount() {
        return idleConnections.size();
    }

    public synchronized int getActiveConnectionCount() {
        return activeConnections.size();
    }

}
