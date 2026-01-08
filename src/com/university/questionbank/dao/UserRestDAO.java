package com.university.questionbank.dao;

import com.university.questionbank.model.User;
import com.university.questionbank.model.Role;
import com.university.questionbank.util.SupabaseRestAPI;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.logging.Logger;

/**
 * 基于 REST API 的用户数据访问对象
 */
public class UserRestDAO implements UserDAO {
    private static final Logger logger = Logger.getLogger(UserRestDAO.class.getName());
    private static final String TABLE = "/users";
    private final Gson gson = new GsonBuilder()
            .serializeNulls()  // 显式序列化 null，可以控制不序列化
            .create();
    private final Gson gsonWithoutNulls = new GsonBuilder()
            .excludeFieldsWithModifiers(java.lang.reflect.Modifier.STATIC)
            .create();

    @Override
    public User getUserByUsername(String username) {
        try {
            String query = "username=eq." + username + "&select=*";
            String response = SupabaseRestAPI.get(TABLE, query);

            if (response == null || response.equals("[]")) {
                logger.warning("用户不存在: " + username);
                return null;
            }

            Type listType = new TypeToken<List<User>>(){}.getType();
            List<User> users = gson.fromJson(response, listType);

            return users.isEmpty() ? null : users.get(0);
        } catch (Exception e) {
            logger.severe("获取用户失败: " + e.getMessage());
            return null;
        }
    }

    @Override
    public User getUserById(int userId) {
        try {
            String query = "user_id=eq." + userId + "&select=*";
            logger.info("从数据库获取用户 ID: " + userId + ", 查询: " + query);
            String response = SupabaseRestAPI.get(TABLE, query);
            logger.info("查询响应: " + (response != null && response.length() > 200 ? response.substring(0, 200) + "..." : response));

            if (response == null || response.equals("[]")) {
                return null;
            }

            Type listType = new TypeToken<List<User>>(){}.getType();
            List<User> users = gson.fromJson(response, listType);

            User user = users.isEmpty() ? null : users.get(0);
            if (user != null) {
                logger.info("用户数据加载成功:");
                logger.info("  - userId: " + user.getUserId());
                logger.info("  - username: " + user.getUsername());
                logger.info("  - realName: " + user.getRealName());
                logger.info("  - roleId: " + user.getRoleId());
            }
            return user;
        } catch (Exception e) {
            logger.severe("获取用户失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<User> getAllUsers() {
        try {
            String query = "select=*&order=created_at.desc";
            String response = SupabaseRestAPI.get(TABLE, query);

            if (response == null || response.equals("[]")) {
                return List.of();
            }

            Type listType = new TypeToken<List<User>>(){}.getType();
            List<User> users = gson.fromJson(response, listType);

            // 为每个用户加载对应的角色信息
            for (User user : users) {
                if (user.getRoleId() > 0) {
                    try {
                        RoleRestDAO roleDAO = new RoleRestDAO();
                        Role role = roleDAO.getRoleById(user.getRoleId());
                        user.setRole(role);
                    } catch (Exception e) {
                        logger.warning("加载用户 " + user.getUsername() + " 的角色失败: " + e.getMessage());
                    }
                }
            }

            return users;
        } catch (Exception e) {
            logger.severe("获取用户列表失败: " + e.getMessage());
            return List.of();
        }
    }

    @Override
    public void addUser(User user) {
        try {
            // 手动构建 JSON，不包含 user_id 字段（让数据库自动生成）
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{");
            jsonBuilder.append("\"username\":\"").append(escapeJson(user.getUsername())).append("\",");
            jsonBuilder.append("\"password\":\"").append(escapeJson(user.getPassword())).append("\",");
            jsonBuilder.append("\"real_name\":\"").append(escapeJson(user.getRealName())).append("\",");
            jsonBuilder.append("\"role_id\":").append(user.getRoleId());
            // 可选字段
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                jsonBuilder.append(",\"email\":\"").append(escapeJson(user.getEmail())).append("\"");
            }
            if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                jsonBuilder.append(",\"phone\":\"").append(escapeJson(user.getPhone())).append("\"");
            }
            if (user.getStatus() != 0) {
                jsonBuilder.append(",\"status\":").append(user.getStatus());
            }
            jsonBuilder.append("}");

            String json = jsonBuilder.toString();
            logger.info("添加用户 JSON: " + json);
            String response = SupabaseRestAPI.post(TABLE, json);
            logger.info("添加用户响应: " + (response != null && response.length() > 200 ? response.substring(0, 200) + "..." : response));

            // Supabase POST 请求在成功时会返回新创建的数据
            // null 表示网络错误
            if (response == null) {
                throw new RuntimeException("网络错误：无法连接到服务器");
            }

            // 空字符串或空数组表示没有数据，但请求成功
            if (response.isEmpty() || response.equals("[]")) {
                logger.info("添加用户成功，但无返回数据");
            } else {
                logger.info("添加用户成功");
            }
        } catch (Exception e) {
            logger.severe("添加用户失败: " + e.getMessage());
            e.printStackTrace();
            throw e;  // 直接抛出原始异常
        }
    }

    @Override
    public void updateUser(User user) {
        try {
            logger.info("准备更新用户:");
            logger.info("  - user.getUserId(): " + user.getUserId());
            logger.info("  - user.getUsername(): " + user.getUsername());
            logger.info("  - user.getRealName(): " + user.getRealName());
            logger.info("  - user.getRoleId(): " + user.getRoleId());

            // 如果 user 对象没有 username 或 username 为 null，从数据库重新获取完整的用户信息
            if (user.getUsername() == null || user.getUsername().isEmpty()) {
                logger.warning("user.getUsername() 为空，从数据库重新获取用户信息");
                User dbUser = getUserById(user.getUserId());
                if (dbUser != null) {
                    user.setUsername(dbUser.getUsername());
                    if (user.getRealName() == null || user.getRealName().isEmpty()) {
                        user.setRealName(dbUser.getRealName());
                    }
                    logger.info("重新获取后 username: " + user.getUsername());
                }
            }

            // PUT 请求需要从数据库获取完整的用户信息
            User existingUser = getUserById(user.getUserId());
            if (existingUser == null) {
                throw new RuntimeException("用户不存在: " + user.getUserId());
            }

            // 构建 JSON，包含所有必要字段（PUT 请求会替换整条记录）
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{");
            jsonBuilder.append("\"user_id\":").append(user.getUserId()).append(",");
            jsonBuilder.append("\"username\":\"").append(escapeJson(user.getUsername())).append("\",");
            jsonBuilder.append("\"real_name\":\"").append(escapeJson(user.getRealName())).append("\",");
            jsonBuilder.append("\"role_id\":").append(user.getRoleId());

            // 密码处理：如果提供了新密码使用新密码，否则使用原密码
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                jsonBuilder.append(",\"password\":\"").append(escapeJson(user.getPassword())).append("\"");
            } else {
                // 使用原密码
                if (existingUser.getPassword() != null) {
                    jsonBuilder.append(",\"password\":\"").append(escapeJson(existingUser.getPassword())).append("\"");
                }
            }

            // 可选字段
            if (existingUser.getEmail() != null && !existingUser.getEmail().isEmpty()) {
                jsonBuilder.append(",\"email\":\"").append(escapeJson(existingUser.getEmail())).append("\"");
            }
            if (existingUser.getPhone() != null && !existingUser.getPhone().isEmpty()) {
                jsonBuilder.append(",\"phone\":\"").append(escapeJson(existingUser.getPhone())).append("\"");
            }
            if (existingUser.getStatus() != 0) {
                jsonBuilder.append(",\"status\":").append(existingUser.getStatus());
            }

            jsonBuilder.append("}");

            String json = jsonBuilder.toString();
            logger.info("更新用户 ID: " + user.getUserId());
            logger.info("更新用户 JSON: " + json);
            String response = SupabaseRestAPI.patch(TABLE + "?user_id=eq." + user.getUserId(), json);
            logger.info("响应内容: " + (response != null ? response : "null"));

            // Supabase PATCH 请求在成功时可能返回空字符串或空数组
            // null 表示网络错误，空字符串表示成功但无返回数据
            if (response == null) {
                throw new RuntimeException("网络错误：无法连接到服务器");
            }

            // 空字符串或空数组都是成功的响应
            if (!response.isEmpty() && !response.equals("[]")) {
                logger.info("更新成功，返回数据: " + response);
            } else {
                logger.info("更新成功，无返回数据");
            }
        } catch (Exception e) {
            logger.severe("更新用户失败: " + e.getMessage());
            e.printStackTrace();
            throw e;  // 直接抛出原始异常，不包装
        }
    }

    // 转义 JSON 字符串中的特殊字符
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                 .replace("\"", "\\\"")
                 .replace("\n", "\\n")
                 .replace("\r", "\\r")
                 .replace("\t", "\\t");
    }

    @Override
    public void deleteUser(int userId) {
        try {
            String query = "user_id=eq." + userId;
            logger.info("删除用户 ID: " + userId);
            String response = SupabaseRestAPI.delete(TABLE + "?" + query);
            logger.info("删除用户响应: " + (response != null && response.length() > 200 ? response.substring(0, 200) + "..." : response));

            // null 表示网络错误
            if (response == null) {
                throw new RuntimeException("网络错误：无法连接到服务器");
            }

            // DELETE 请求通常返回空数组表示成功
            if (response.isEmpty() || response.equals("[]")) {
                logger.info("删除用户成功");
            } else {
                logger.info("删除用户成功，返回数据: " + response);
            }
        } catch (Exception e) {
            logger.severe("删除用户失败: " + e.getMessage());
            e.printStackTrace();
            throw e;  // 直接抛出原始异常
        }
    }

    @Override
    public void updateUserStatus(int userId, int status) {
        try {
            String query = "user_id=eq." + userId;
            String json = "{\"status\":" + status + "}";
            logger.info("更新用户状态 ID: " + userId + ", 状态: " + status);
            String response = SupabaseRestAPI.patch(TABLE + "?" + query, json);
            logger.info("更新用户状态响应: " + (response != null && response.length() > 200 ? response.substring(0, 200) + "..." : response));

            // null 表示网络错误
            if (response == null) {
                throw new RuntimeException("网络错误：无法连接到服务器");
            }

            // 空字符串或空数组都是成功的响应
            if (response.isEmpty() || response.equals("[]")) {
                logger.info("更新用户状态成功，无返回数据");
            } else {
                logger.info("更新用户状态成功，返回数据: " + response);
            }
        } catch (Exception e) {
            logger.severe("更新用户状态失败: " + e.getMessage());
            e.printStackTrace();
            throw e;  // 直接抛出原始异常
        }
    }

    // 添加辅助方法用于登录
    public User login(String username, String password) {
        try {
            User user = getUserByUsername(username);
            if (user == null) {
                return null;
            }

            // 验证密码
            if (com.university.questionbank.util.PasswordUtil.checkPassword(password, user.getPassword())) {
                logger.info("用户登录成功: " + username);
                return user;
            } else {
                logger.warning("密码错误: " + username);
                return null;
            }
        } catch (Exception e) {
            logger.severe("登录失败: " + e.getMessage());
            return null;
        }
    }
}
