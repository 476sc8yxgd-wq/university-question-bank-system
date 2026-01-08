package com.university.questionbank.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * 网络环境适配器
 * 检测网络环境和端口可用性
 */
public class NetworkAdapter {
    private static final Logger logger = LoggerFactory.getLogger(NetworkAdapter.class);
    
    // 默认端口超时时间（毫秒）
    private static final int SOCKET_TIMEOUT = 5000;
    
    /**
     * 端口可用性测试结果
     */
    public static class PortAvailability {
        private final int port;
        private final boolean available;
        private final long latency;
        private final String message;
        
        public PortAvailability(int port, boolean available, long latency, String message) {
            this.port = port;
            this.available = available;
            this.latency = latency;
            this.message = message;
        }
        
        public int getPort() {
            return port;
        }
        
        public boolean isAvailable() {
            return available;
        }
        
        public long getLatency() {
            return latency;
        }
        
        public String getMessage() {
            return message;
        }
        
        @Override
        public String toString() {
            if (available) {
                return String.format("端口 %d 可用，延迟 %d ms", port, latency);
            } else {
                return String.format("端口 %d 不可用: %s", port, message);
            }
        }
    }
    
    /**
     * 检测所有常用端口的可用性
     */
    public static List<PortAvailability> checkAllPorts(String host) {
        List<Integer> ports = ConnectionTester.getAllTestPorts();
        return checkPorts(host, ports);
    }
    
    /**
     * 检测指定端口的可用性
     */
    public static PortAvailability checkPort(String host, int port) {
        Socket socket = null;
        long startTime = System.currentTimeMillis();
        
        try {
            logger.info("检查端口 {} 可用性: {}:{}", host, port);
            
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), SOCKET_TIMEOUT);
            
            long latency = System.currentTimeMillis() - startTime;
            logger.info("端口 {} 可用，延迟 {} ms", port, latency);
            
            return new PortAvailability(port, true, latency, "端口可访问");
            
        } catch (IOException e) {
            long latency = System.currentTimeMillis() - startTime;
            String message = e.getMessage();
            
            logger.warn("端口 {} 不可用: {} (耗时 {} ms)", port, message, latency);
            
            return new PortAvailability(port, false, latency, message);
            
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    logger.error("关闭 socket 失败", e);
                }
            }
        }
    }
    
    /**
     * 批量检测端口可用性
     */
    public static List<PortAvailability> checkPorts(String host, List<Integer> ports) {
        List<PortAvailability> results = new ArrayList<>();
        
        for (int port : ports) {
            PortAvailability availability = checkPort(host, port);
            results.add(availability);
        }
        
        return results;
    }
    
    /**
     * 获取推荐的端口
     * 策略：优先选择延迟最低且可用的端口
     */
    public static int getRecommendedPort(String host) {
        List<PortAvailability> availabilities = checkAllPorts(host);
        
        int recommendedPort = ConnectionTester.DEFAULT_PORT;
        long minLatency = Long.MAX_VALUE;
        
        for (PortAvailability availability : availabilities) {
            if (availability.isAvailable() && availability.getLatency() < minLatency) {
                minLatency = availability.getLatency();
                recommendedPort = availability.getPort();
            }
        }
        
        logger.info("推荐端口: {} (延迟: {} ms)", recommendedPort, minLatency);
        
        return recommendedPort;
    }
    
    /**
     * 检查端口是否被阻止
     */
    public static boolean isPortBlocked(String host, int port) {
        PortAvailability availability = checkPort(host, port);
        return !availability.isAvailable();
    }
    
    /**
     * 检查默认端口（5432）是否可用
     */
    public static boolean isDefaultPortAvailable(String host) {
        return !isPortBlocked(host, ConnectionTester.DEFAULT_PORT);
    }
    
    /**
     * 检查连接池端口（6543）是否可用
     */
    public static boolean isPoolPortAvailable(String host) {
        return !isPortBlocked(host, ConnectionTester.POOL_PORT);
    }
    
    /**
     * 获取网络状态摘要
     */
    public static String getNetworkStatus(String host) {
        List<PortAvailability> availabilities = checkAllPorts(host);
        StringBuilder sb = new StringBuilder();
        
        sb.append("网络环境检测报告\n");
        sb.append("==================\n");
        sb.append(String.format("主机: %s\n\n", host));
        
        for (PortAvailability availability : availabilities) {
            sb.append(availability.toString()).append("\n");
        }
        
        sb.append("\n推荐端口: ").append(getRecommendedPort(host));
        
        return sb.toString();
    }
    
    /**
     * 获取最佳连接端口
     * 综合考虑可用性和延迟
     */
    public static int getBestPort(String host) {
        List<PortAvailability> availabilities = checkAllPorts(host);
        
        // 优先级：连接池端口 > 默认端口
        Integer priorityPort = ConnectionTester.POOL_PORT;
        
        // 检查连接池端口是否可用
        for (PortAvailability availability : availabilities) {
            if (availability.getPort() == priorityPort && availability.isAvailable()) {
                return priorityPort;
            }
        }
        
        // 如果连接池端口不可用，使用默认端口
        for (PortAvailability availability : availabilities) {
            if (availability.getPort() == ConnectionTester.DEFAULT_PORT && availability.isAvailable()) {
                return ConnectionTester.DEFAULT_PORT;
            }
        }
        
        // 都不可用，返回连接池端口（让数据库连接失败）
        logger.warn("所有端口都不可用，默认返回连接池端口");
        return ConnectionTester.POOL_PORT;
    }
}
