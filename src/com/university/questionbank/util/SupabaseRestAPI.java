package com.university.questionbank.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Supabase REST API 工具类
 * 通过 HTTPS 端口访问 Supabase 数据库
 */
public class SupabaseRestAPI {
    private static final Logger logger = Logger.getLogger(SupabaseRestAPI.class.getName());

    private static final String SUPABASE_URL = "https://tjvwymicbizzfibfjvej.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRqdnd5bWljYml6emZpYmZqdmVqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjY3NTAxODQsImV4cCI6MjA4MjMyNjE4NH0.0ZM7WYp1V-o-5h3GubL5rlY6l2sFOo58VpK0xJ3NF2w";
    private static final String API_BASE = SUPABASE_URL + "/rest/v1";

    private static final int CONNECT_TIMEOUT = 30000;
    private static final int READ_TIMEOUT = 30000;

    /**
     * 发送 HTTP GET 请求
     */
    public static String get(String endpoint, String query) {
        try {
            String url = API_BASE + endpoint;
            if (query != null && !query.isEmpty()) {
                url += "?" + query;
            }

            logger.info("GET 请求: " + url);
            HttpURLConnection conn = createConnection(url, "GET");
            conn.setRequestProperty("apikey", SUPABASE_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_KEY);

            return readResponse(conn);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "GET 请求失败", e);
            e.printStackTrace();
            throw new RuntimeException("HTTP GET 请求失败: " + e.getMessage(), e);
        }
    }

    /**
     * 发送 HTTP POST 请求
     */
    public static String post(String endpoint, String jsonBody) {
        HttpURLConnection conn = null;
        try {
            String url = API_BASE + endpoint;
            logger.info("POST 请求: " + url);
            logger.info("请求体: " + jsonBody);

            conn = createConnection(url, "POST");
            conn.setRequestProperty("apikey", SUPABASE_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Prefer", "return=representation");

            if (jsonBody != null && !jsonBody.isEmpty()) {
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                    os.flush();
                }
            }

            String response = readResponse(conn);
            logger.info("POST 成功，响应: " + (response != null && response.length() > 200 ? response.substring(0, 200) + "..." : response));
            return response;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "POST 请求失败", e);
            e.printStackTrace();
            throw new RuntimeException("HTTP POST 请求失败: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * 发送 HTTP PATCH 请求
     * 注意：由于 Java HttpURLConnection 不支持 PATCH 方法，我们使用 PUT 代替
     */
    public static String patch(String endpoint, String jsonBody) {
        HttpURLConnection conn = null;
        try {
            String url = API_BASE + endpoint;
            logger.info("PATCH 请求 (使用 PUT): " + url);
            logger.info("请求体: " + jsonBody);

            conn = createConnection(url, "PUT");
            conn.setRequestProperty("apikey", SUPABASE_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Prefer", "return=representation");

            if (jsonBody != null && !jsonBody.isEmpty()) {
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                    os.flush();
                }
            }

            String response = readResponse(conn);
            logger.info("PUT 成功，响应: " + (response != null && response.length() > 200 ? response.substring(0, 200) + "..." : response));
            return response;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "PUT 请求失败", e);
            e.printStackTrace();
            // 不要返回 null，应该重新抛出异常让调用方处理
            throw new RuntimeException("HTTP PUT 请求失败: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * 发送 HTTP DELETE 请求
     */
    public static String delete(String endpoint) {
        try {
            String url = API_BASE + endpoint;
            logger.info("DELETE 请求: " + url);

            HttpURLConnection conn = createConnection(url, "DELETE");
            conn.setRequestProperty("apikey", SUPABASE_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_KEY);

            return readResponse(conn);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "DELETE 请求失败", e);
            e.printStackTrace();
            throw new RuntimeException("HTTP DELETE 请求失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建 HTTP 连接
     */
    private static HttpURLConnection createConnection(String urlString, String method) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod(method);
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        return conn;
    }

    /**
     * 读取响应
     */
    private static String readResponse(HttpURLConnection conn) throws Exception {
        int responseCode = conn.getResponseCode();
        logger.info("响应码: " + responseCode);

        BufferedReader reader = null;
        try {
            if (responseCode >= 200 && responseCode < 300) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            } else {
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            // 如果无法获取输入流，创建空响应
            logger.warning("无法读取响应流: " + e.getMessage());
            if (responseCode >= 200 && responseCode < 300) {
                return "";
            } else {
                throw new RuntimeException("HTTP " + responseCode + ": 无法读取错误信息");
            }
        }

        StringBuilder response = new StringBuilder();
        String line;
        try {
            if (reader != null) {
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }
        } catch (Exception e) {
            logger.warning("读取响应内容时出错: " + e.getMessage());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception e) {
                logger.warning("关闭读取器时出错: " + e.getMessage());
            }
        }

        String responseStr = response.toString();
        logger.info("响应内容: " + (responseStr.length() > 200 ? responseStr.substring(0, 200) + "..." : responseStr));

        if (responseCode >= 200 && responseCode < 300) {
            return responseStr;
        } else {
            throw new RuntimeException("HTTP " + responseCode + ": " + responseStr);
        }
    }

    /**
     * 测试连接
     */
    public static boolean testConnection() {
        try {
            // 查询用户表来测试连接
            String response = get("/users", "limit=1");
            return response != null;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "连接测试失败", e);
            return false;
        }
    }
}
