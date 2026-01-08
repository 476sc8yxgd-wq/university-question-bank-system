package com.university.questionbank.dao;

import com.university.questionbank.model.Question;

import java.sql.SQLException;
import java.util.List;

public interface QuestionDAO {
    // 添加题目
    void addQuestion(Question question) throws SQLException;

    // 更新题目
    void updateQuestion(Question question) throws SQLException;

    // 删除题目
    void deleteQuestion(int questionId) throws SQLException;

    // 根据题目ID获取题目
    Question getQuestionById(int questionId) throws SQLException;

    // 根据创建者ID获取题目列表
    List<Question> getQuestionsByCreatorId(int creatorId) throws SQLException;

    // 获取所有题目
    List<Question> getAllQuestions() throws SQLException;
    
    // 获取所有题目（分页）
    List<Question> getAllQuestions(int offset, int limit) throws SQLException;

    // 根据条件搜索题目
    List<Question> searchQuestions(String keyword, Integer categoryId, Integer difficultyId, String questionType) throws SQLException;
    
    // 根据条件搜索题目（分页）
    List<Question> searchQuestions(String keyword, Integer categoryId, Integer difficultyId, String questionType, int offset, int limit) throws SQLException;
}