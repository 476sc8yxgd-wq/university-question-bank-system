package com.university.questionbank.service;

import com.university.questionbank.dao.*;
import com.university.questionbank.dao.DAOFactory;
import com.university.questionbank.model.*;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class QuestionService {
    private static final Logger logger = Logger.getLogger(QuestionService.class.getName());
    private QuestionDAO questionDAO;
    private QuestionCategoryDAO categoryDAO;
    private QuestionDifficultyDAO difficultyDAO;
    private UserDAO userDAO;

    public QuestionService() {
        this.questionDAO = DAOFactory.createQuestionDAO();
        this.categoryDAO = DAOFactory.createQuestionCategoryDAO();
        this.difficultyDAO = DAOFactory.createQuestionDifficultyDAO();
        this.userDAO = DAOFactory.createUserDAO();
    }

    // 题目管理
    public void addQuestion(Question question) throws SQLException {
        questionDAO.addQuestion(question);
    }

    public void updateQuestion(Question question) throws SQLException {
        questionDAO.updateQuestion(question);
    }

    public void deleteQuestion(int questionId) throws SQLException {
        questionDAO.deleteQuestion(questionId);
    }

    public Question getQuestionById(int questionId) throws SQLException {
        return questionDAO.getQuestionById(questionId);
    }

    public List<Question> getQuestionsByCreatorId(int creatorId) throws SQLException {
        return questionDAO.getQuestionsByCreatorId(creatorId);
    }

    public List<Question> getAllQuestions() throws SQLException {
        return questionDAO.getAllQuestions();
    }

    public List<Question> searchQuestions(String keyword, Integer categoryId, Integer difficultyId, String questionType) throws SQLException {
        return questionDAO.searchQuestions(keyword, categoryId, difficultyId, questionType);
    }
    
    // 获取所有题目（分页）
    public List<Question> getAllQuestions(int offset, int limit) throws SQLException {
        return questionDAO.getAllQuestions(offset, limit);
    }
    
    // 根据条件搜索题目（分页）
    public List<Question> searchQuestions(String keyword, Integer categoryId, Integer difficultyId, String questionType, int offset, int limit) throws SQLException {
        return questionDAO.searchQuestions(keyword, categoryId, difficultyId, questionType, offset, limit);
    }

    // 批量导入题目
    public void importQuestions(List<Question> questions) throws SQLException {
        if (questions == null || questions.isEmpty()) {
            return;
        }
        
        for (Question question : questions) {
            questionDAO.addQuestion(question);
        }
    }

    // 分类管理
    public void addCategory(QuestionCategory category) throws SQLException {
        categoryDAO.addCategory(category);
    }

    public void updateCategory(QuestionCategory category) throws SQLException {
        categoryDAO.updateCategory(category);
    }

    public void deleteCategory(int categoryId) throws SQLException {
        // 检查是否有题目使用该分类
        List<Question> questions = questionDAO.searchQuestions(null, categoryId, null, null);
        if (!questions.isEmpty()) {
            throw new SQLException("该分类下存在题目，无法删除");
        }
        categoryDAO.deleteCategory(categoryId);
    }

    public QuestionCategory getCategoryById(int categoryId) throws SQLException {
        return categoryDAO.getCategoryById(categoryId);
    }

    public QuestionCategory getCategoryByName(String categoryName) throws SQLException {
        return categoryDAO.getCategoryByName(categoryName);
    }

    public List<QuestionCategory> getAllCategories() throws SQLException {
        return categoryDAO.getAllCategories();
    }

    // 难度管理
    public void addDifficulty(QuestionDifficulty difficulty) throws SQLException {
        difficultyDAO.addDifficulty(difficulty);
    }

    public void updateDifficulty(QuestionDifficulty difficulty) throws SQLException {
        difficultyDAO.updateDifficulty(difficulty);
    }

    public void deleteDifficulty(int difficultyId) throws SQLException {
        // 检查是否有题目使用该难度
        List<Question> questions = questionDAO.searchQuestions(null, null, difficultyId, null);
        if (!questions.isEmpty()) {
            throw new SQLException("该难度下存在题目，无法删除");
        }
        difficultyDAO.deleteDifficulty(difficultyId);
    }

    public QuestionDifficulty getDifficultyById(int difficultyId) throws SQLException {
        return difficultyDAO.getDifficultyById(difficultyId);
    }

    public List<QuestionDifficulty> getAllDifficulties() throws SQLException {
        return difficultyDAO.getAllDifficulties();
    }
    
    // 获取所有用户
    public List<User> getAllUsers() throws SQLException {
        return userDAO.getAllUsers();
    }

    // 辅助方法：根据ID获取分类名称
    public String getCategoryNameById(int categoryId) {
        try {
            logger.info("获取分类名称: categoryId=" + categoryId);
            QuestionCategory category = categoryDAO.getCategoryById(categoryId);
            String name = category != null ? category.getCategoryName() : "";
            logger.info("分类名称: " + name);
            return name;
        } catch (Exception e) {
            logger.severe("获取分类名称失败: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    // 辅助方法：根据ID获取难度名称
    public String getDifficultyLevelById(int difficultyId) {
        try {
            logger.info("获取难度名称: difficultyId=" + difficultyId);
            QuestionDifficulty difficulty = difficultyDAO.getDifficultyById(difficultyId);
            String name = difficulty != null ? difficulty.getDifficultyLevel() : "";
            logger.info("难度名称: " + name);
            return name;
        } catch (Exception e) {
            logger.severe("获取难度名称失败: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    // 辅助方法：根据ID获取用户名称
    public String getUserNameById(int userId) {
        try {
            logger.info("获取用户名称: userId=" + userId);
            User user = userDAO.getUserById(userId);
            String name = user != null ? user.getRealName() : "";
            logger.info("用户名称: " + name);
            return name;
        } catch (Exception e) {
            logger.severe("获取用户名称失败: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }
}