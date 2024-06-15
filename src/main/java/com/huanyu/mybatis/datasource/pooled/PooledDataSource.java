package com.huanyu.mybatis.datasource.pooled;

import com.huanyu.mybatis.datasource.unpooled.UnpooledDataSource;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.logging.Logger;

/**
 * ClassName: PooledDataSource
 * Package: com.huanyu.mybatis.datasource.pooled
 * Description: 有连接池的数据源
 *
 * @Author: 寰宇
 * @Create: 2024/6/14 12:31
 * @Version: 1.0
 */
public class PooledDataSource implements DataSource {

    private org.slf4j.Logger logger = LoggerFactory.getLogger(PooledDataSource.class);

    // 池状态
    private final PoolState state = new PoolState(this);

    // 持有一个UnpooledDataSource对象
    private final UnpooledDataSource dataSource;

    // 活跃连接数
    protected int poolMaximumActiveConnections = 10;
    // 空闲连接数
    protected int poolMaximumIdleConnections = 5;
    // 在被强制返回之前,池中连接被检查的时间
    protected int poolMaximumCheckoutTime = 20000;
    // 这是给连接池一个打印日志状态机会的低层次设置,还有重新尝试获得连接, 这些情况下往往需要很长时间 为了避免连接池没有配置时静默失败)。
    protected int poolTimeToWait = 20000;
    // 发送到数据的侦测查询,用来验证连接是否正常工作,并且准备 接受请求。默认是“NO PING QUERY SET” ,这会引起许多数据库驱动连接由一 个错误信息而导致失败
    protected String poolPingQuery = "NO PING QUERY SET";
    // 开启或禁用侦测查询
    protected boolean poolPingEnabled = false;
    // 用来配置 poolPingQuery 多次时间被用一次
    protected int poolPingConnectionsNotUsedFor = 0;

    // 存储池子中的连接的编码，编码用("" + url + username + password).hashCode()算出来
    // 因此，整个池子中的所有连接的编码必须是一致的，里面的连接是等价的
    private int expectedConnectionTypeCode;

    public PooledDataSource() {
        this.dataSource = new UnpooledDataSource();
    }

    /**
     * 收回一个连接
     * @param connection 连接
     * @throws SQLException
     */
    protected void pushConnection(PooledConnection connection) throws SQLException {
        synchronized (state) {
            // 将该连接从活跃连接中删除
            state.activeConnections.remove(connection);
            // 判断连接是否有效
            if (connection.isValid()) { // 当前连接是可用的
                // 判断连接池未满 + 该连接确实属于该连接池
                if (state.idleConnections.size() < poolMaximumIdleConnections && connection.getConnectionTypeCode() == expectedConnectionTypeCode) {
                    state.accumulatedCheckoutTime += connection.getCheckoutTime();
                    // 它首先检查数据库连接是否处于自动提交模式，如果不是，则调用rollback()方法执行回滚操作。
                    // 在MyBatis中，如果没有开启自动提交模式，则需要手动提交或回滚事务。因此，这段代码可能是在确保操作完成后，如果没有开启自动提交模式，则执行回滚操作。
                    // 总的来说，这段代码用于保证数据库的一致性，确保操作完成后，如果未开启自动提交模式，则执行回滚操作。
                    if (!connection.getRealConnection().getAutoCommit()) { // 如果连接没有设置自动提交
                        // 将未完成的操作回滚
                        connection.getRealConnection().rollback();
                    }
                    // 实例化一个新的DB连接，加入到idle列表
                    PooledConnection newConnection = new PooledConnection(connection.getRealConnection(), this);
                    // 将连接放入空闲连接池
                    state.idleConnections.add(newConnection);
                    newConnection.setCreatedTimestamp(connection.getCreatedTimestamp());
                    newConnection.setLastUsedTimestamp(connection.getLastUsedTimestamp());
                    // 设置连接为未校验，以便取出时重新校验
                    connection.invalidate();
                    logger.info("Returned connection " + newConnection.getRealHashCode() + " to pool.");

                    // 通知其他线程可以来抢DB连接了
                    state.notifyAll();
                }
                // 连接池已满或者该连接不属于该连接池
                else {
                    state.accumulatedCheckoutTime += connection.getCheckoutTime();
                    if (!connection.getRealConnection().getAutoCommit()) {
                        connection.getRealConnection().rollback();
                    }
                    // 将connection关闭
                    connection.getRealConnection().close();
                    logger.info("Closed connection " + connection.getRealHashCode() + ".");
                    connection.invalidate();
                }
            } else { // 当前连接不可用
                logger.info("A bad connection (" + connection.getRealHashCode() + ") attempted to return to the pool, discarding connection.");
                state.badConnectionCount++;
            }
        }
    }

    /**
     * 从池化数据源中给出一个连接
     * @param username 用户名
     * @param password 密码
     * @return 池化的数据库连接
     * @throws SQLException
     */
    private PooledConnection popConnection(String username, String password) throws SQLException {
        boolean countedWait = false;
        PooledConnection conn = null;
        long t = System.currentTimeMillis();
        // 用于统计取出连接花费的时长的时间起点
        int localBadConnectionCount = 0;

        while (conn == null) {
            // 给state加同步锁
            synchronized (state) {
                // 如果有空闲连接：返回第一个
                if (!state.idleConnections.isEmpty()) {
                    // 左移操作，取出第一个连接
                    conn = state.idleConnections.remove(0);
                    logger.info("Checked out connection " + conn.getRealHashCode() + " from pool.");
                }
                // 如果无空闲连接：创建新的连接
                else {
                    // 池中还有空余位置
                    if (state.activeConnections.size() < poolMaximumActiveConnections) {
                        // 可以创建新连接，也是通过DriverManager.getConnection拿到的连接
                        conn = new PooledConnection(dataSource.getConnection(), this);
                        logger.info("Created connection " + conn.getRealHashCode() + ".");
                    }
                    // 活跃连接数已满
                    else {
                        // 取得活跃连接列表的第一个，也就是最老的一个连接
                        PooledConnection oldestActiveConnection = state.activeConnections.get(0);
                        // 查看借出去最久的连接已经被借了多久
                        long longestCheckoutTime = oldestActiveConnection.getCheckoutTime();
                        // 如果checkout时间过长，则这个连接标记为过期
                        if (longestCheckoutTime > poolMaximumCheckoutTime) { // 借出时间超过设定的借出时长
                            // 声明该连接超期不还
                            state.claimedOverdueConnectionCount++;
                            state.accumulatedCheckoutTimeOfOverdueConnections += longestCheckoutTime;
                            state.accumulatedCheckoutTime += longestCheckoutTime;
                            // 因超期不还而从池中除名
                            state.activeConnections.remove(oldestActiveConnection);
                            if (!oldestActiveConnection.getRealConnection().getAutoCommit()) { // 如果超期不还的连接没有设置自动提交事务
                                // 尝试替它提交回滚事务
                                try {
                                    oldestActiveConnection.getRealConnection().rollback();
                                } catch (SQLException e) {
                                    // 即使替它回滚事务的操作失败，也不抛出异常，仅仅做一下记录
                                    logger.info("Bad connection. Could not roll back");
                                }
                            }
                            // 新建一个连接替代超期不还连接的位置
                            conn = new PooledConnection(oldestActiveConnection.getRealConnection(), this);
                            oldestActiveConnection.invalidate();
                            logger.info("Claimed overdue connection " + conn.getRealHashCode() + ".");
                        }
                        // 如果checkout超时时间不够长，则等待
                        else {
                            // 继续等待，等待有连接归还到连接池
                            try {
                                if (!countedWait) {
                                    // 记录发生等待的次数。某次请求等待多轮也只能算作发生了一次等待
                                    state.hadToWaitCount++;
                                    countedWait = true;
                                }
                                logger.info("Waiting as long as " + poolTimeToWait + " milliseconds for connection.");
                                long wt = System.currentTimeMillis();
                                // 沉睡一段时间再试，防止一直占有计算资源
                                state.wait(poolTimeToWait);
                                state.accumulatedWaitTime += System.currentTimeMillis() - wt;
                            } catch (InterruptedException e) {
                                break;
                            }
                        }

                    }
                }
                // 获得到连接
                if (conn != null) {
                    // 判断连接是否可用
                    if (conn.isValid()) { // 如果连接可用
                        if (!conn.getRealConnection().getAutoCommit()) { // 该连接没有设置自动提交
                            // 回滚未提交的操作
                            conn.getRealConnection().rollback();
                        }
                        // 每个借出去的连接都到打上数据源的连接类型编码，以便在归还时确保正确
                        conn.setConnectionTypeCode(assembleConnectionTypeCode(dataSource.getUrl(), username, password));
                        // 数据记录操作
                        conn.setCheckoutTimestamp(System.currentTimeMillis());
                        conn.setLastUsedTimestamp(System.currentTimeMillis());
                        // 加入活跃连接池中
                        state.activeConnections.add(conn);
                        state.requestCount++;
                        state.accumulatedRequestTime += System.currentTimeMillis() - t;
                    } else { // 连接不可用
                        logger.info("A bad connection (" + conn.getRealHashCode() + ") was returned from the pool, getting another connection.");
                        // 如果没拿到，统计信息：失败连接 +1
                        state.badConnectionCount++;
                        localBadConnectionCount++;
                        // 直接删除连接
                        conn = null;
                        // 失败次数较多，抛异常
                        if (localBadConnectionCount > (poolMaximumIdleConnections + 3)) {
                            logger.debug("PooledDataSource: Could not get a good connection to the database.");
                            throw new SQLException("PooledDataSource: Could not get a good connection to the database.");
                        }
                    }
                }
            }
            // 如果到这里还没拿到连接，则会循环此过程，继续尝试取连接
        }

        if (conn == null) {
            logger.debug("PooledDataSource: Unknown severe error condition.  The connection pool returned a null connection.");
            throw new SQLException("PooledDataSource: Unknown severe error condition.  The connection pool returned a null connection.");
        }

        return conn;
    }

    /**
     * 将活动和空闲的连接全部关闭
     */
    public void forceCloseAll() {
        synchronized (state) { // 增加同步锁
            // 重新计算和更新连接类型编码
            expectedConnectionTypeCode = assembleConnectionTypeCode(dataSource.getUrl(), dataSource.getUsername(), dataSource.getPassword());
            // 关闭活跃连接
            for (int i = state.activeConnections.size(); i > 0; i--) {
                try {
                    PooledConnection conn = state.activeConnections.remove(i - 1);
                    conn.invalidate();

                    Connection realConn = conn.getRealConnection();
                    if (!realConn.getAutoCommit()) {
                        realConn.rollback();
                    }
                    realConn.close();
                } catch (Exception ignore) {

                }
            }
            // 关闭空闲连接
            for (int i = state.idleConnections.size(); i > 0; i--) {
                try {
                    PooledConnection conn = state.idleConnections.remove(i - 1);
                    conn.invalidate();

                    Connection realConn = conn.getRealConnection();
                    if (!realConn.getAutoCommit()) {
                        realConn.rollback();
                    }
                } catch (Exception ignore) {

                }
            }
            logger.info("PooledDataSource forcefully closed/removed all connections.");
        }
    }

    protected boolean pingConnection(PooledConnection conn) {
        boolean result = true;

        try {
            result = !conn.getRealConnection().isClosed();
        } catch (SQLException e) {
            logger.info("Connection " + conn.getRealHashCode() + " is BAD: " + e.getMessage());
            result = false;
        }

        if (result) {
            if (poolPingEnabled) {
                if (poolPingConnectionsNotUsedFor >= 0 && conn.getTimeElapsedSinceLastUse() > poolPingConnectionsNotUsedFor) {
                    try {
                        logger.info("Testing connection " + conn.getRealHashCode() + " ...");
                        Connection realConn = conn.getRealConnection();
                        Statement statement = realConn.createStatement();
                        ResultSet resultSet = statement.executeQuery(poolPingQuery);
                        resultSet.close();
                        if (!realConn.getAutoCommit()) {
                            realConn.rollback();
                        }
                        result = true;
                        logger.info("Connection " + conn.getRealHashCode() + " is GOOD!");
                    } catch (Exception e) {
                        logger.info("Execution of ping query '" + poolPingQuery + "' failed: " + e.getMessage());
                        try {
                            conn.getRealConnection().close();
                        } catch (SQLException ignore) {
                        }
                        result = false;
                        logger.info("Connection " + conn.getRealHashCode() + " is BAD: " + e.getMessage());
                    }
                }
            }
        }

        return result;
    }

    public static Connection unwrapConnection(Connection conn) {
        if (Proxy.isProxyClass(conn.getClass())) {
            InvocationHandler handler = Proxy.getInvocationHandler(conn);
            if (handler instanceof PooledConnection) {
                return ((PooledConnection) handler).getRealConnection();
            }
        }
        return conn;
    }

    /**
     * 计算该连接池中连接的类型编码
     * @param url 连接地址
     * @param username 用户名
     * @param password 密码
     * @return 类型编码
     */
    private int assembleConnectionTypeCode(String url, String username, String password) {
        return ("" + url + username + password).hashCode();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return popConnection(dataSource.getUsername(), dataSource.getPassword()).getProxyConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return popConnection(username, password).getProxyConnection();
    }

    protected void finalize() throws Throwable {
        forceCloseAll();
        super.finalize();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException(getClass().getName() + " is not a wrapper.");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return DriverManager.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter logWriter) throws SQLException {
        DriverManager.setLogWriter(logWriter);
    }

    @Override
    public void setLoginTimeout(int loginTimeout) throws SQLException {
        DriverManager.setLoginTimeout(loginTimeout);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return DriverManager.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }

    public void setDriver(String driver) {
        dataSource.setDriver(driver);
        forceCloseAll();
    }

    public void setUrl(String url) {
        dataSource.setUrl(url);
        forceCloseAll();
    }

    public void setUsername(String username) {
        dataSource.setUsername(username);
        forceCloseAll();
    }

    public void setPassword(String password) {
        dataSource.setPassword(password);
        forceCloseAll();
    }


    public void setDefaultAutoCommit(boolean defaultAutoCommit) {
        dataSource.setAutoCommit(defaultAutoCommit);
        forceCloseAll();
    }

    public int getPoolMaximumActiveConnections() {
        return poolMaximumActiveConnections;
    }

    public void setPoolMaximumActiveConnections(int poolMaximumActiveConnections) {
        this.poolMaximumActiveConnections = poolMaximumActiveConnections;
    }

    public int getPoolMaximumIdleConnections() {
        return poolMaximumIdleConnections;
    }

    public void setPoolMaximumIdleConnections(int poolMaximumIdleConnections) {
        this.poolMaximumIdleConnections = poolMaximumIdleConnections;
    }

    public int getPoolMaximumCheckoutTime() {
        return poolMaximumCheckoutTime;
    }

    public void setPoolMaximumCheckoutTime(int poolMaximumCheckoutTime) {
        this.poolMaximumCheckoutTime = poolMaximumCheckoutTime;
    }

    public int getPoolTimeToWait() {
        return poolTimeToWait;
    }

    public void setPoolTimeToWait(int poolTimeToWait) {
        this.poolTimeToWait = poolTimeToWait;
    }

    public String getPoolPingQuery() {
        return poolPingQuery;
    }

    public void setPoolPingQuery(String poolPingQuery) {
        this.poolPingQuery = poolPingQuery;
    }

    public boolean isPoolPingEnabled() {
        return poolPingEnabled;
    }

    public void setPoolPingEnabled(boolean poolPingEnabled) {
        this.poolPingEnabled = poolPingEnabled;
    }

    public int getPoolPingConnectionsNotUsedFor() {
        return poolPingConnectionsNotUsedFor;
    }

    public void setPoolPingConnectionsNotUsedFor(int poolPingConnectionsNotUsedFor) {
        this.poolPingConnectionsNotUsedFor = poolPingConnectionsNotUsedFor;
    }

    public int getExpectedConnectionTypeCode() {
        return expectedConnectionTypeCode;
    }

    public void setExpectedConnectionTypeCode(int expectedConnectionTypeCode) {
        this.expectedConnectionTypeCode = expectedConnectionTypeCode;
    }

}
