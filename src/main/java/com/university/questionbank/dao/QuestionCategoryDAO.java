package com.university.questionbank.dao;

import com.university.questionbank.model.QuestionCategory;

import java.sql.SQLException;
import java.util.List;

public interface QuestionCategoryDAO {
    // 添加题目分类
    void addCategory(QuestionCategory category) throws SQLException;

    // 更新题目分类
    void updateCategory(QuestionCategory category) throws SQLException;

    // 删除题目分类
    void deleteCategory(int categoryId) throws SQLException;

    // 根据分类ID获取分类
    QuestionCategory getCategoryById(int categoryId) throws SQLException;

    // 根据分类名称获取分类
    QuestionCategory getCategoryByName(String categoryName) throws SQLException;

    // 获取所有题目分类
    List<QuestionCategory> getAllCategories() throws SQLException;
}