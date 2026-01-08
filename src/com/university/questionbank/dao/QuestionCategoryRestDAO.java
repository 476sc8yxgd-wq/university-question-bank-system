package com.university.questionbank.dao;

import com.university.questionbank.model.QuestionCategory;
import com.university.questionbank.util.SupabaseRestAPI;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/**
 * 基于 REST API 的题目分类数据访问对象
 */
public class QuestionCategoryRestDAO implements QuestionCategoryDAO {
    private static final Logger logger = Logger.getLogger(QuestionCategoryRestDAO.class.getName());
    private static final String TABLE = "/question_categories";
    private final Gson gson = new Gson();

    @Override
    public void addCategory(QuestionCategory category) throws SQLException {
        try {
            // 构建JSON时不包含category_id，让数据库自动生成ID
            String json = gson.toJson(category);
            // 移除category_id字段，让数据库自动生成
            json = json.replaceAll("\"category_id\":\\s*\\d+,?", "");
            json = json.replaceAll(",\\s*}", "}");  // 移除末尾的逗号

            logger.info("添加分类 - JSON: " + json);

            String response = SupabaseRestAPI.post(TABLE, json);
            logger.info("添加分类后 - 响应: " + response);

            // 解析返回的新 ID
            if (response != null && !response.isEmpty()) {
                try {
                    // Supabase 返回的是数组格式
                    if (response.startsWith("[") && response.endsWith("]")) {
                        response = response.substring(1, response.length() - 1);
                    }
                    // 解析返回的分类对象，获取新的 ID
                    QuestionCategory newCategory = gson.fromJson(response, QuestionCategory.class);
                    if (newCategory != null && newCategory.getCategoryId() > 0) {
                        category.setCategoryId(newCategory.getCategoryId());
                        logger.info("成功解析新分类ID: " + category.getCategoryId());
                    } else {
                        logger.warning("无法解析新分类ID，newCategory为null或ID<=0");
                        throw new SQLException("无法解析新分类ID");
                    }
                } catch (Exception e) {
                    logger.warning("解析新分类ID失败: " + e.getMessage());
                    e.printStackTrace();
                    throw new SQLException("解析新分类ID失败: " + e.getMessage());
                }
            } else {
                logger.warning("响应为空或null");
                throw new SQLException("添加分类失败：服务器未返回响应");
            }
        } catch (SQLException e) {
            logger.severe("添加分类失败: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.severe("添加分类失败: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException(e);
        }
    }

    @Override
    public void updateCategory(QuestionCategory category) throws SQLException {
        try {
            String query = "category_id=eq." + category.getCategoryId();
            String json = gson.toJson(category);
            String response = SupabaseRestAPI.patch(TABLE + "?" + query, json);
            if (response == null || response.isEmpty()) {
                throw new SQLException("更新分类失败");
            }
        } catch (Exception e) {
            logger.severe("更新分类失败: " + e.getMessage());
            throw new SQLException(e);
        }
    }

    @Override
    public void deleteCategory(int categoryId) throws SQLException {
        try {
            String query = "category_id=eq." + categoryId;
            String response = SupabaseRestAPI.delete(TABLE + "?" + query);
            if (response == null) {
                throw new SQLException("删除分类失败");
            }
        } catch (Exception e) {
            logger.severe("删除分类失败: " + e.getMessage());
            throw new SQLException(e);
        }
    }

    @Override
    public QuestionCategory getCategoryById(int categoryId) throws SQLException {
        try {
            String query = "category_id=eq." + categoryId + "&select=*";
            String response = SupabaseRestAPI.get(TABLE, query);

            if (response == null || response.equals("[]")) {
                return null;
            }

            Type listType = new TypeToken<List<QuestionCategory>>(){}.getType();
            List<QuestionCategory> categories = gson.fromJson(response, listType);

            return categories.isEmpty() ? null : categories.get(0);
        } catch (Exception e) {
            logger.severe("获取分类失败: " + e.getMessage());
            throw new SQLException(e);
        }
    }

    @Override
    public QuestionCategory getCategoryByName(String categoryName) throws SQLException {
        try {
            String encodedName = URLEncoder.encode(categoryName, StandardCharsets.UTF_8);
            String query = "category_name=eq." + encodedName + "&select=*";
            String response = SupabaseRestAPI.get(TABLE, query);

            if (response == null || response.equals("[]")) {
                return null;
            }

            Type listType = new TypeToken<List<QuestionCategory>>(){}.getType();
            List<QuestionCategory> categories = gson.fromJson(response, listType);

            return categories.isEmpty() ? null : categories.get(0);
        } catch (Exception e) {
            logger.severe("获取分类失败: " + e.getMessage());
            throw new SQLException(e);
        }
    }

    @Override
    public List<QuestionCategory> getAllCategories() throws SQLException {
        try {
            String query = "select=*&order=category_id";
            String response = SupabaseRestAPI.get(TABLE, query);

            if (response == null || response.equals("[]")) {
                return List.of();
            }

            Type listType = new TypeToken<List<QuestionCategory>>(){}.getType();
            return gson.fromJson(response, listType);
        } catch (Exception e) {
            logger.severe("获取分类列表失败: " + e.getMessage());
            throw new SQLException(e);
        }
    }
}
