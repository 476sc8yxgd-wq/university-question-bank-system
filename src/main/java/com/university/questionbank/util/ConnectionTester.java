package com.university.questionbank.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 数据库连接测试工具
 * 支持测试多个端口，自动选择可用的连接方案
 */
public class ConnectionTester {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionTester.class);
    
    // 默认测试的超时时间（毫秒）
    private static final int DEFAULT_TIMEOUT = 10000;
    
    // Supabase 默认端口
    public static final int DEFAULT_PORT = 5432;
    
    // Supabase 连接池端口
    public static final int POOL_PORT = 6543;
    
    /**
     * 连接测试结果
     */
    public static class ConnectionTestResult {
        private final int port;
        private final boolean success;
        private final long duration;
        private final String errorMessage;
        private final Connection connection;
        
        public ConnectionTestResult(int port, boolean success, long duration, String errorMessage, Connection connection) {
            this.port = port;
            this.success = success;
            this.duration = duration;
            this.errorMessage = errorMessage;
            this.connection = connection;
        }
        
        public int getPort() {
            return port;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public long getDuration() {
            return duration;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public Connection getConnection() {
            return connection;
        }
        
        @Override
        public String toString() {
            if (success) {
                return String.format("端口 %d 连接成功，耗时 %d ms", port, duration);
            } else {
                return String.format("端口 %d 连接失败: %s", port, errorMessage);
            }
        }
    }
    
    /**
     * 测试所有常用端口，返回第一个成功的连接
     */
    public static Connection testAllPorts(String host, String database, String username, String password) {
        List<Integer> ports = new ArrayList<>();
        ports.add(DEFAULT_PORT);
        ports.add(POOL_PORT);
        
        return testPorts(host, database, username, password, ports);
    }
    
    /**
     * 测试指定的端口列表，返回第一个成功的连接
     */
    public static Connection testPorts(String host, String database, String username, String password, List<Integer> ports) {
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(ports.size(), 4));
        List<Future<ConnectionTestResult>> futures = new ArrayList<>();
        
        logger.info("开始测试 {} 个端口: {}", ports.size(), ports);
        
        // 异步测试所有端口
        for (int port : ports) {
            final int portNumber = port;
            Future<ConnectionTestResult> future = executor.submit(() -> {
                return testSinglePort(host, portNumber, database, username, password, DEFAULT_TIMEOUT);
            });
            futures.add(future);
        }
        
        // 等待第一个成功的连接
        for (Future<ConnectionTestResult> future : futures) {
            try {
                ConnectionTestResult result = future.get(DEFAULT_TIMEOUT + 2000, TimeUnit.MILLISECONDS);
                logger.info("端口 {} 测试结果: {}", result.getPort(), result);
                
                if (result.isSuccess()) {
                    // 关闭其他未完成的任务
                    for (Future<ConnectionTestResult> f : futures) {
                        if (f != future && !f.isDone()) {
                            f.cancel(true);
                        }
                    }
                    
                    executor.shutdown();
                    return result.getConnection();
                }
            } catch (TimeoutException e) {
                logger.warn("端口测试超时");
                future.cancel(true);
            } catch (Exception e) {
                logger.error("端口测试异常", e);
            }
        }
        
        executor.shutdown();
        return null;
    }
    
    /**
     * 测试单个端口
     */
    public static ConnectionTestResult testSinglePort(String host, int port, String database, String username, String password) {
        return testSinglePort(host, port, database, username, password, DEFAULT_TIMEOUT);
    }
    
    /**
     * 测试单个端口（带超时）
     */
    public static ConnectionTestResult testSinglePort(String host, int port, String database, String username, String password, int timeoutMs) {
        String url = String.format("jdbc:postgresql://%s:%d/%s?sslmode=require", host, port, database);
        long startTime = System.currentTimeMillis();
        
        try {
            logger.info("尝试连接端口 {}: {}", port, url);
            
            // 设置连接超时
            Connection conn = DriverManager.getConnection(url, username, password);
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("端口 {} 连接成功，耗时 {} ms", port, duration);
            
            return new ConnectionTestResult(port, true, duration, null, conn);
            
        } catch (SQLException e) {
            long duration = System.currentTimeMillis() - startTime;
            String errorMessage = e.getMessage();
            
            logger.error("端口 {} 连接失败 (耗时 {} ms): {}", port, duration, errorMessage);
            
            return new ConnectionTestResult(port, false, duration, errorMessage, null);
        }
    }
    
    /**
     * 测试默认端口（5432）
     */
    public static ConnectionTestResult testDefaultPort(String host, String database, String username, String password) {
        return testSinglePort(host, DEFAULT_PORT, database, username, password, DEFAULT_TIMEOUT);
    }
    
    /**
     * 测试连接池端口（6543）
     */
    public static ConnectionTestResult testPoolPort(String host, String database, String username, String password) {
        return testSinglePort(host, POOL_PORT, database, username, password, DEFAULT_TIMEOUT);
    }
    
    /**
     * 批量测试端口，返回所有测试结果
     */
    public static List<ConnectionTestResult> testAllPortsDetailed(String host, String database, String username, String password) {
        List<Integer> ports = new ArrayList<>();
        ports.add(DEFAULT_PORT);
        ports.add(POOL_PORT);
        
        return testPortsDetailed(host, database, username, password, ports);
    }
    
    /**
     * 批量测试指定端口，返回所有测试结果
     */
    public static List<ConnectionTestResult> testPortsDetailed(String host, String database, String username, String password, List<Integer> ports) {
        List<ConnectionTestResult> results = new ArrayList<>();
        
        for (int port : ports) {
            ConnectionTestResult result = testSinglePort(host, port, database, username, password, DEFAULT_TIMEOUT);
            results.add(result);
            
            // 如果成功，关闭连接
            if (result.isSuccess() && result.getConnection() != null) {
                try {
                    result.getConnection().close();
                } catch (SQLException e) {
                    logger.error("关闭连接失败", e);
                }
            }
        }
        
        return results;
    }
    
    /**
     * 获取推荐端口
     * 优先返回连接池端口（6543），因为通常性能更好
     */
    public static int getRecommendedPort() {
        return POOL_PORT;
    }
    
    /**
     * 获取所有测试端口列表
     */
    public static List<Integer> getAllTestPorts() {
        List<Integer> ports = new ArrayList<>();
        ports.add(DEFAULT_PORT);
        ports.add(POOL_PORT);
        return ports;
    }
}
