package com.university.questionbank.dao;

import com.university.questionbank.model.QuestionDifficulty;
import com.university.questionbank.util.SupabaseRestAPI;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/**
 * 基于 REST API 的题目难度数据访问对象
 */
public class QuestionDifficultyRestDAO implements QuestionDifficultyDAO {
    private static final Logger logger = Logger.getLogger(QuestionDifficultyRestDAO.class.getName());
    private static final String TABLE = "/question_difficulties";
    private final Gson gson = new Gson();

    @Override
    public void addDifficulty(QuestionDifficulty difficulty) throws SQLException {
        try {
            // 构建JSON时不包含difficulty_id，让数据库自动生成ID
            String json = gson.toJson(difficulty);
            // 移除difficulty_id字段，让数据库自动生成
            json = json.replaceAll("\"difficulty_id\":\\s*\\d+,?", "");
            json = json.replaceAll(",\\s*}", "}");  // 移除末尾的逗号

            logger.info("添加难度，JSON: " + json);

            String response = SupabaseRestAPI.post(TABLE, json);
            logger.info("添加难度响应: " + response);

            // 解析返回的新 ID
            if (response != null && !response.isEmpty()) {
                try {
                    // Supabase 返回的是数组格式
                    if (response.startsWith("[") && response.endsWith("]")) {
                        response = response.substring(1, response.length() - 1);
                    }
                    // 解析返回的难度对象，获取新的 ID
                    QuestionDifficulty newDifficulty = gson.fromJson(response, QuestionDifficulty.class);
                    if (newDifficulty != null && newDifficulty.getDifficultyId() > 0) {
                        difficulty.setDifficultyId(newDifficulty.getDifficultyId());
                        logger.info("新难度ID: " + difficulty.getDifficultyId());
                    } else {
                        logger.warning("无法解析新难度ID");
                        throw new SQLException("无法解析新难度ID");
                    }
                } catch (Exception e) {
                    logger.warning("解析新难度ID失败: " + e.getMessage());
                    e.printStackTrace();
                    throw new SQLException("解析新难度ID失败: " + e.getMessage());
                }
            } else {
                logger.warning("响应为空或null");
                throw new SQLException("添加难度失败：服务器未返回响应");
            }
        } catch (SQLException e) {
            logger.severe("添加难度失败: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.severe("添加难度失败: " + e.getMessage());
            throw new SQLException(e);
        }
    }

    @Override
    public void updateDifficulty(QuestionDifficulty difficulty) throws SQLException {
        try {
            String query = "difficulty_id=eq." + difficulty.getDifficultyId();
            String json = gson.toJson(difficulty);
            String response = SupabaseRestAPI.patch(TABLE + "?" + query, json);
            if (response == null || response.isEmpty()) {
                throw new SQLException("更新难度失败");
            }
        } catch (Exception e) {
            logger.severe("更新难度失败: " + e.getMessage());
            throw new SQLException(e);
        }
    }

    @Override
    public void deleteDifficulty(int difficultyId) throws SQLException {
        try {
            String query = "difficulty_id=eq." + difficultyId;
            String response = SupabaseRestAPI.delete(TABLE + "?" + query);
            if (response == null) {
                throw new SQLException("删除难度失败");
            }
        } catch (Exception e) {
            logger.severe("删除难度失败: " + e.getMessage());
            throw new SQLException(e);
        }
    }

    @Override
    public QuestionDifficulty getDifficultyById(int difficultyId) throws SQLException {
        try {
            String query = "difficulty_id=eq." + difficultyId + "&select=*";
            String response = SupabaseRestAPI.get(TABLE, query);

            if (response == null || response.equals("[]")) {
                return null;
            }

            Type listType = new TypeToken<List<QuestionDifficulty>>(){}.getType();
            List<QuestionDifficulty> difficulties = gson.fromJson(response, listType);

            return difficulties.isEmpty() ? null : difficulties.get(0);
        } catch (Exception e) {
            logger.severe("获取难度失败: " + e.getMessage());
            throw new SQLException(e);
        }
    }

    @Override
    public List<QuestionDifficulty> getAllDifficulties() throws SQLException {
        try {
            String query = "select=*&order=difficulty_id";
            String response = SupabaseRestAPI.get(TABLE, query);

            if (response == null || response.equals("[]")) {
                return List.of();
            }

            Type listType = new TypeToken<List<QuestionDifficulty>>(){}.getType();
            return gson.fromJson(response, listType);
        } catch (Exception e) {
            logger.severe("获取难度列表失败: " + e.getMessage());
            throw new SQLException(e);
        }
    }
}
