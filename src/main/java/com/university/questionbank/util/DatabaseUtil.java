package com.university.questionbank.util;

import com.university.questionbank.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class DatabaseUtil {
    private static final Logger logger = Logger.getLogger(DatabaseUtil.class.getName());

    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            DatabaseConfig config = DatabaseConfig.getInstance();
            String url = config.getJdbcUrl();
            String username = config.getUsername();
            String password = config.getPassword();

            logger.info("正在连接数据库: " + url);

            try {
                if (config.isSQLite()) {
                    // SQLite 不需要用户名和密码
                    connection = DriverManager.getConnection(url);
                } else {
                    // PostgreSQL 需要用户名和密码
                    connection = DriverManager.getConnection(url, username, password);
                }
                logger.info("数据库连接成功");
            } catch (SQLException e) {
                logger.severe("数据库连接失败: " + e.getMessage());
                throw e;
            }
        }
        return connection;
    }

    public static boolean testConnection() throws SQLException {
        try {
            Connection conn = getConnection();
            if (conn != null) {
                conn.close();
                return true;
            }
            return false;
        } catch (SQLException e) {
            logger.warning("数据库连接测试失败: " + e.getMessage());
            throw e;
        }
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                logger.info("数据库连接已关闭");
            } catch (SQLException e) {
                logger.warning("关闭数据库连接失败: " + e.getMessage());
            }
            connection = null;
        }
    }
}
