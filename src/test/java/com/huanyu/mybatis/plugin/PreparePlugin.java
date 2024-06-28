package com.huanyu.mybatis.plugin;

import com.huanyu.mybatis.executor.statement.StatementHandler;

import java.sql.Connection;
import java.util.Properties;

//连接中获取一个Statement的时间,超过阈值则记录SQL

// 拦截器注解:拦截的类、类中的方法、参数
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class})})
public class PreparePlugin implements Interceptor {

    private long threshold; // 阈值，超过该时间的SQL会被记录

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        long startTime = System.currentTimeMillis(); // 记录开始时间
        Object result = invocation.proceed(); // 执行原方法
        long endTime = System.currentTimeMillis(); // 记录结束时间
        long time = endTime - startTime; // 计算耗时

        // 判断是否超过阈值
        if (time > threshold) {
            // 获取StatementHandler
            StatementHandler handler = (StatementHandler) invocation.getTarget();
            // 获取SQL信息
            String sql = handler.getBoundSql().getSql();
            System.out.println("Slow SQL (" + time + " ms): " + sql);
        }

        return result;
    }

    @Override
    public void setProperties(Properties properties) {
        String thresholdStr = properties.getProperty("threshold");
        this.threshold = Long.parseLong(thresholdStr); // 设置阈值
    }

}
