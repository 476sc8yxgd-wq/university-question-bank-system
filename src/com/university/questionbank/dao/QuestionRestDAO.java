package com.university.questionbank.dao;

import com.university.questionbank.model.Question;
import com.university.questionbank.model.QuestionCategory;
import com.university.questionbank.model.QuestionDifficulty;
import com.university.questionbank.model.User;
import com.university.questionbank.util.SupabaseRestAPI;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 基于 REST API 的题目数据访问对象
 */
public class QuestionRestDAO implements QuestionDAO {
    private static final Logger logger = Logger.getLogger(QuestionRestDAO.class.getName());
    private static final String TABLE = "/questions";
    private final Gson gson = new Gson();
    private final QuestionCategoryDAO categoryDAO;
    private final QuestionDifficultyDAO difficultyDAO;
    private final UserDAO userDAO;

    // 用户缓存，避免重复查询
    private final Map<Integer, User> userCache = new HashMap<>();
    // 分类缓存，避免N+1查询
    private final Map<Integer, QuestionCategory> categoryCache = new HashMap<>();
    // 难度缓存，避免N+1查询
    private final Map<Integer, QuestionDifficulty> difficultyCache = new HashMap<>();
    
    // 缓存是否已加载的标志
    private boolean cachesLoaded = false;

    public QuestionRestDAO() {
        this.categoryDAO = DAOFactory.createQuestionCategoryDAO();
        this.difficultyDAO = DAOFactory.createQuestionDifficultyDAO();
        this.userDAO = DAOFactory.createUserDAO();
    }

    @Override
    public void addQuestion(Question question) {
        try {
            // 构建JSON时不包含question_id，让数据库自动生成ID
            String json = gson.toJson(question);
            // 移除question_id字段，让数据库自动生成
            json = json.replaceAll("\"question_id\":\\s*\\d+,?", "");
            json = json.replaceAll(",\\s*}", "}");  // 移除末尾的逗号

            logger.info("添加题目，JSON: " + json);

            String response = SupabaseRestAPI.post(TABLE, json);
            logger.info("添加题目响应: " + response);

            if (response == null) {
                throw new RuntimeException("添加题目失败：服务器未返回响应");
            }

            // 检查是否是错误响应（409 错误等）
            if (response.contains("\"code\"") || response.contains("duplicate key")) {
                logger.severe("添加题目失败，服务器返回: " + response);
                throw new RuntimeException("添加题目失败: " + response);
            }

            // 尝试解析返回的题目ID
            try {
                if (response.startsWith("[") && response.endsWith("]")) {
                    response = response.substring(1, response.length() - 1);
                }
                // 解析返回的题目对象，获取新的 ID
                Question newQuestion = gson.fromJson(response, Question.class);
                if (newQuestion != null && newQuestion.getQuestionId() > 0) {
                    question.setQuestionId(newQuestion.getQuestionId());
                    logger.info("新题目ID: " + question.getQuestionId());
                } else {
                    logger.warning("无法解析新题目ID");
                    throw new RuntimeException("无法解析新题目ID");
                }
            } catch (Exception e) {
                logger.warning("解析响应失败: " + e.getMessage());
                throw new RuntimeException("解析响应失败: " + e.getMessage());
            }

        } catch (RuntimeException e) {
            // 捕获并显示Supabase返回的详细错误信息
            logger.severe("添加题目失败: " + e.getMessage());
            throw new RuntimeException("添加题目失败: " + e.getMessage());
        } catch (Exception e) {
            logger.severe("添加题目失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("添加题目失败: " + e.getMessage());
        }
    }

    @Override
    public void updateQuestion(Question question) {
        try {
            String query = "question_id=eq." + question.getQuestionId();
            String json = gson.toJson(question);
            String response = SupabaseRestAPI.patch(TABLE + "?" + query, json);
            if (response == null || response.isEmpty()) {
                throw new RuntimeException("更新题目失败");
            }
        } catch (Exception e) {
            logger.severe("更新题目失败: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteQuestion(int questionId) {
        try {
            String query = "question_id=eq." + questionId;
            String response = SupabaseRestAPI.delete(TABLE + "?" + query);
            if (response == null) {
                throw new RuntimeException("删除题目失败");
            }
        } catch (Exception e) {
            logger.severe("删除题目失败: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public Question getQuestionById(int questionId) {
        try {
            String query = "question_id=eq." + questionId + "&select=*";
            String response = SupabaseRestAPI.get(TABLE, query);

            if (response == null || response.equals("[]")) {
                return null;
            }

            Type listType = new TypeToken<List<Question>>(){}.getType();
            List<Question> questions = gson.fromJson(response, listType);

            if (!questions.isEmpty()) {
                Question question = questions.get(0);
                // 使用缓存填充关联对象
                loadCachesIfNeeded();
                
                if (question.getCategoryId() > 0) {
                    question.setCategory(categoryCache.get(question.getCategoryId()));
                }
                if (question.getDifficultyId() > 0) {
                    question.setDifficulty(difficultyCache.get(question.getDifficultyId()));
                }
                if (question.getCreatorId() > 0) {
                    question.setCreator(getUserFromCache(question.getCreatorId()));
                }
                return question;
            }
            return null;
        } catch (Exception e) {
            logger.severe("获取题目失败: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Question> getQuestionsByCreatorId(int creatorId) {
        try {
            String query = "creator_id=eq." + creatorId + "&select=*&order=created_at.desc";
            String response = SupabaseRestAPI.get(TABLE, query);

            if (response == null || response.equals("[]")) {
                return List.of();
            }

            Type listType = new TypeToken<List<Question>>(){}.getType();
            List<Question> questions = gson.fromJson(response, listType);
            // 使用批量填充优化性能
            populateCategoriesAndDifficultiesBatch(questions);
            return questions;
        } catch (Exception e) {
            logger.severe("获取创建者题目失败: " + e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<Question> getAllQuestions() throws SQLException {
        return getAllQuestions(100); // 默认限制100条记录
    }
    
    /**
     * 获取所有题目，支持分页限制
     * @param limit 返回的最大记录数
     * @return 题目列表
     */
    public List<Question> getAllQuestions(int limit) throws SQLException {
        return getAllQuestions(0, limit);
    }
    
    @Override
    public List<Question> getAllQuestions(int offset, int limit) throws SQLException {
        try {
            String query = "select=*&order=created_at.desc&offset=" + offset + "&limit=" + limit;
            String response = SupabaseRestAPI.get(TABLE, query);

            if (response == null || response.equals("[]")) {
                return List.of();
            }

            Type listType = new TypeToken<List<Question>>(){}.getType();
            List<Question> questions = gson.fromJson(response, listType);
            // 使用批量填充优化性能
            populateCategoriesAndDifficultiesBatch(questions);
            return questions;
        } catch (Exception e) {
            logger.severe("获取题目列表失败: " + e.getMessage());
            throw new SQLException("获取题目列表失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Question> searchQuestions(String keyword, Integer categoryId, Integer difficultyId, String questionType) throws SQLException {
        return searchQuestions(keyword, categoryId, difficultyId, questionType, 100); // 默认限制100条记录
    }
    
    /**
     * 搜索题目，支持分页限制
     * @param keyword 关键词
     * @param categoryId 分类ID
     * @param difficultyId 难度ID
     * @param questionType 题目类型
     * @param limit 返回的最大记录数
     * @return 题目列表
     */
    public List<Question> searchQuestions(String keyword, Integer categoryId, Integer difficultyId, String questionType, int limit) throws SQLException {
        return searchQuestions(keyword, categoryId, difficultyId, questionType, 0, limit);
    }
    
    @Override
    public List<Question> searchQuestions(String keyword, Integer categoryId, Integer difficultyId, String questionType, int offset, int limit) throws SQLException {
        try {
            StringBuilder query = new StringBuilder("select=*");

            if (keyword != null && !keyword.isEmpty()) {
                // 对关键词进行 URL 编码
                String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
                query.append("&or=(question_content.ilike.*").append(encodedKeyword).append("*)");
            }

            if (categoryId != null) {
                query.append("&category_id=eq.").append(categoryId);
            }

            if (difficultyId != null) {
                query.append("&difficulty_id=eq.").append(difficultyId);
            }

            if (questionType != null && !questionType.isEmpty()) {
                // 对题目类型进行 URL 编码
                String encodedQuestionType = URLEncoder.encode(questionType, StandardCharsets.UTF_8);
                query.append("&question_type=eq.").append(encodedQuestionType);
            }

            query.append("&order=created_at.desc");
            query.append("&offset=").append(offset);
            query.append("&limit=").append(limit);

            logger.info("搜索题目查询参数: " + query.toString());
            String response = SupabaseRestAPI.get(TABLE, query.toString());

            if (response == null || response.equals("[]")) {
                return List.of();
            }

            Type listType = new TypeToken<List<Question>>(){}.getType();
            List<Question> questions = gson.fromJson(response, listType);
            // 使用批量填充优化性能
            populateCategoriesAndDifficultiesBatch(questions);
            return questions;
        } catch (Exception e) {
            logger.severe("搜索题目失败: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * 填充题目对象中的分类、难度和创建者对象
     */
    private void populateCategoryAndDifficulty(Question question) {
        if (question == null) {
            return;
        }

        try {
            // 确保缓存已加载
            loadCachesIfNeeded();
            
            // 填充分类对象（从缓存获取）
            if (question.getCategoryId() > 0) {
                QuestionCategory category = categoryCache.get(question.getCategoryId());
                question.setCategory(category);
            }

            // 填充难度对象（从缓存获取）
            if (question.getDifficultyId() > 0) {
                QuestionDifficulty difficulty = difficultyCache.get(question.getDifficultyId());
                question.setDifficulty(difficulty);
            }

            // 填充创建者对象（使用缓存避免重复查询）
            if (question.getCreatorId() > 0) {
                User creator = getUserFromCache(question.getCreatorId());
                question.setCreator(creator);
            }
        } catch (Exception e) {
            logger.warning("填充题目关联对象失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量填充题目对象中的分类、难度和创建者对象
     */
    private void populateCategoriesAndDifficultiesBatch(List<Question> questions) {
        if (questions == null || questions.isEmpty()) {
            return;
        }
        
        try {
            // 确保缓存已加载
            loadCachesIfNeeded();
            
            // 批量填充分类、难度和创建者对象
            for (Question question : questions) {
                // 填充分类对象（从缓存获取）
                if (question.getCategoryId() > 0) {
                    QuestionCategory category = categoryCache.get(question.getCategoryId());
                    question.setCategory(category);
                }
                
                // 填充难度对象（从缓存获取）
                if (question.getDifficultyId() > 0) {
                    QuestionDifficulty difficulty = difficultyCache.get(question.getDifficultyId());
                    question.setDifficulty(difficulty);
                }
                
                // 填充创建者对象（使用缓存避免重复查询）
                if (question.getCreatorId() > 0) {
                    User creator = getUserFromCache(question.getCreatorId());
                    question.setCreator(creator);
                }
            }
        } catch (Exception e) {
            logger.warning("批量填充题目关联对象失败: " + e.getMessage());
        }
    }

    /**
     * 加载分类和难度缓存（如果需要）
     */
    private void loadCachesIfNeeded() {
        if (!cachesLoaded) {
            try {
                // 加载所有分类到缓存
                List<QuestionCategory> allCategories = categoryDAO.getAllCategories();
                for (QuestionCategory category : allCategories) {
                    categoryCache.put(category.getCategoryId(), category);
                }
                
                // 加载所有难度到缓存
                List<QuestionDifficulty> allDifficulties = difficultyDAO.getAllDifficulties();
                for (QuestionDifficulty difficulty : allDifficulties) {
                    difficultyCache.put(difficulty.getDifficultyId(), difficulty);
                }
                
                cachesLoaded = true;
                logger.info("分类和难度缓存已加载，分类数: " + categoryCache.size() + ", 难度数: " + difficultyCache.size());
            } catch (SQLException e) {
                logger.warning("加载分类和难度缓存失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 从缓存获取用户，避免重复查询
     */
    private User getUserFromCache(int userId) {
        // 检查缓存
        if (userCache.containsKey(userId)) {
            return userCache.get(userId);
        }

        try {
            // 缓存未命中，从数据库查询
            User user = userDAO.getUserById(userId);
            userCache.put(userId, user);
            return user;
        } catch (Exception e) {
            logger.warning("获取用户失败: userId=" + userId + ", " + e.getMessage());
            return null;
        }
    }

    /**
     * 清空用户缓存（用于刷新数据）
     */
    public void clearUserCache() {
        userCache.clear();
    }
    
    /**
     * 清空所有缓存（用于刷新数据）
     */
    public void clearAllCaches() {
        userCache.clear();
        categoryCache.clear();
        difficultyCache.clear();
        cachesLoaded = false;
        logger.info("所有缓存已清空");
    }
}
