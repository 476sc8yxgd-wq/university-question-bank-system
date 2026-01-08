package com.university.questionbank.dao;

import com.university.questionbank.model.QuestionDifficulty;

import java.sql.SQLException;
import java.util.List;

public interface QuestionDifficultyDAO {
    // 添加题目难度
    void addDifficulty(QuestionDifficulty difficulty) throws SQLException;

    // 更新题目难度
    void updateDifficulty(QuestionDifficulty difficulty) throws SQLException;

    // 删除题目难度
    void deleteDifficulty(int difficultyId) throws SQLException;

    // 根据难度ID获取难度信息
    QuestionDifficulty getDifficultyById(int difficultyId) throws SQLException;

    // 获取所有题目难度
    List<QuestionDifficulty> getAllDifficulties() throws SQLException;
}