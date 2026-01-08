package com.university.questionbank.config;

import java.io.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * 数据库配置管理器
 * 支持多端口配置和持久化存储
 */
public class DatabaseConfig {
    private static final Logger logger = Logger.getLogger(DatabaseConfig.class.getName());

    // 配置文件路径
    private static final String CONFIG_FILE = "config/database.properties";

    // 数据库类型
    private static String dbType = System.getProperty("db.type", "postgresql");

    // 配置项键名
    private static final String KEY_HOST = "db.host";
    private static final String KEY_PORT = "db.port";
    private static final String KEY_DATABASE = "db.database";
    private static final String KEY_USERNAME = "db.username";
    private static final String KEY_PASSWORD = "db.password";
    private static final String KEY_SSL_MODE = "db.sslmode";
    private static final String KEY_TIMEOUT = "db.timeout";
    private static final String KEY_AUTO_TEST = "db.autoTest";

    // 默认配置值
    private static final String DEFAULT_HOST = "tjvwymicbizzfibfjvej.supabase.co";
    private static final int DEFAULT_PORT = 5432;
    private static final String DEFAULT_DATABASE = "postgres";
    private static final String DEFAULT_USERNAME = "postgres";
    private static final String DEFAULT_PASSWORD = "3SwxyyWNIDMj56FB";
    private static final String DEFAULT_SSL_MODE = "require";
    private static final int DEFAULT_TIMEOUT = 30000;
    private static final boolean DEFAULT_AUTO_TEST = true;

    private static DatabaseConfig instance;

    private Properties properties;
    private int currentPort;
    private String currentJdbcUrl;

    public DatabaseConfig() {
        properties = new Properties();
        loadConfig();

        // 如果是 SQLite 模式，使用系统属性中的 URL
        if ("sqlite".equals(dbType)) {
            currentJdbcUrl = System.getProperty("db.url", "jdbc:sqlite:question_bank.db");
            logger.info("使用 SQLite 模式: " + currentJdbcUrl);
        } else {
            // 初始化当前端口为默认端口
            currentPort = getPort();
            currentJdbcUrl = buildJdbcUrl(currentPort);
        }
    }

    public static DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    public boolean isSQLite() {
        return "sqlite".equals(dbType);
    }

    public String getHost() {
        return properties.getProperty(KEY_HOST, DEFAULT_HOST);
    }

    public int getPort() {
        return Integer.parseInt(properties.getProperty(KEY_PORT, String.valueOf(DEFAULT_PORT)));
    }

    public String getDatabase() {
        return properties.getProperty(KEY_DATABASE, DEFAULT_DATABASE);
    }

    public String getUsername() {
        return properties.getProperty(KEY_USERNAME, DEFAULT_USERNAME);
    }

    public String getPassword() {
        return properties.getProperty(KEY_PASSWORD, DEFAULT_PASSWORD);
    }

    public String getSslMode() {
        return properties.getProperty(KEY_SSL_MODE, DEFAULT_SSL_MODE);
    }

    public int getTimeout() {
        return Integer.parseInt(properties.getProperty(KEY_TIMEOUT, String.valueOf(DEFAULT_TIMEOUT)));
    }

    public boolean getAutoTest() {
        return Boolean.parseBoolean(properties.getProperty(KEY_AUTO_TEST, String.valueOf(DEFAULT_AUTO_TEST)));
    }

    public String getJdbcUrl() {
        return currentJdbcUrl;
    }

    public void loadConfig() {
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            try (InputStream input = new FileInputStream(configFile)) {
                properties.load(input);
                logger.info("已加载配置文件: " + CONFIG_FILE);
            } catch (IOException e) {
                logger.warning("加载配置文件失败，使用默认配置: " + e.getMessage());
                useDefaults();
            }
        } else {
            logger.info("配置文件不存在，使用默认配置");
            useDefaults();
        }
    }

    private void useDefaults() {
        properties.setProperty(KEY_HOST, DEFAULT_HOST);
        properties.setProperty(KEY_PORT, String.valueOf(DEFAULT_PORT));
        properties.setProperty(KEY_DATABASE, DEFAULT_DATABASE);
        properties.setProperty(KEY_USERNAME, DEFAULT_USERNAME);
        properties.setProperty(KEY_PASSWORD, DEFAULT_PASSWORD);
        properties.setProperty(KEY_SSL_MODE, DEFAULT_SSL_MODE);
        properties.setProperty(KEY_TIMEOUT, String.valueOf(DEFAULT_TIMEOUT));
        properties.setProperty(KEY_AUTO_TEST, String.valueOf(DEFAULT_AUTO_TEST));
    }

    private String buildJdbcUrl(int port) {
        return String.format("jdbc:postgresql://%s:%d/%s?sslmode=require",
            getHost(), port, getDatabase());
    }

    public void setPort(int port) {
        this.currentPort = port;
        this.currentJdbcUrl = buildJdbcUrl(port);
        properties.setProperty(KEY_PORT, String.valueOf(port));
    }

    public void setHost(String host) {
        properties.setProperty(KEY_HOST, host);
        this.currentJdbcUrl = buildJdbcUrl(currentPort);
    }

    public void setDatabase(String database) {
        properties.setProperty(KEY_DATABASE, database);
        this.currentJdbcUrl = buildJdbcUrl(currentPort);
    }

    public void setUsername(String username) {
        properties.setProperty(KEY_USERNAME, username);
    }

    public void setPassword(String password) {
        properties.setProperty(KEY_PASSWORD, password);
    }

    public void setSslMode(String sslMode) {
        properties.setProperty(KEY_SSL_MODE, sslMode);
        this.currentJdbcUrl = buildJdbcUrl(currentPort);
    }

    public void setTimeout(int timeout) {
        properties.setProperty(KEY_TIMEOUT, String.valueOf(timeout));
    }

    /**
     * 保存配置到文件
     */
    public void saveConfig() {
        File configFile = new File(CONFIG_FILE);
        File configDir = configFile.getParentFile();

        if (configDir != null && !configDir.exists()) {
            configDir.mkdirs();
        }

        try (OutputStream output = new FileOutputStream(configFile)) {
            properties.store(output, "Database Configuration");
            logger.info("配置已保存到: " + CONFIG_FILE);
        } catch (IOException e) {
            logger.severe("保存配置文件失败: " + e.getMessage());
            throw new RuntimeException("保存配置文件失败", e);
        }
    }

    /**
     * 获取配置摘要
     */
    public String getConfigSummary() {
        return String.format(
            "主机: %s\n" +
            "端口: %d\n" +
            "数据库: %s\n" +
            "用户名: %s\n" +
            "SSL模式: %s\n" +
            "超时: %d ms",
            getHost(),
            getPort(),
            getDatabase(),
            getUsername(),
            getSslMode(),
            getTimeout()
        );
    }
}
