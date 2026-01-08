package com.university.questionbank.dao.impl;

import com.university.questionbank.dao.QuestionCategoryDAO;
import com.university.questionbank.model.QuestionCategory;
import com.university.questionbank.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuestionCategoryDAOImpl implements QuestionCategoryDAO {

    @Override
    public void addCategory(QuestionCategory category) throws SQLException {
        String sql = "INSERT INTO question_categories (category_name, description) VALUES (?, ?);";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, category.getCategoryName());
            pstmt.setString(2, category.getDescription());
            pstmt.executeUpdate();
        }
    }

    @Override
    public void updateCategory(QuestionCategory category) throws SQLException {
        String sql = "UPDATE question_categories SET category_name = ?, description = ? WHERE category_id = ?;";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, category.getCategoryName());
            pstmt.setString(2, category.getDescription());
            pstmt.setInt(3, category.getCategoryId());
            pstmt.executeUpdate();
        }
    }

    @Override
    public void deleteCategory(int categoryId) throws SQLException {
        String sql = "DELETE FROM question_categories WHERE category_id = ?;";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, categoryId);
            pstmt.executeUpdate();
        }
    }

    @Override
    public QuestionCategory getCategoryById(int categoryId) throws SQLException {
        String sql = "SELECT * FROM question_categories WHERE category_id = ?;";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, categoryId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToCategory(rs);
            }
        }
        return null;
    }

    @Override
    public QuestionCategory getCategoryByName(String categoryName) throws SQLException {
        String sql = "SELECT * FROM question_categories WHERE category_name = ?;";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, categoryName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToCategory(rs);
            }
        }
        return null;
    }

    @Override
    public List<QuestionCategory> getAllCategories() throws SQLException {
        List<QuestionCategory> categories = new ArrayList<>();
        String sql = "SELECT * FROM question_categories ORDER BY category_id;";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
        }
        return categories;
    }

    // 将ResultSet映射为QuestionCategory对象
    private QuestionCategory mapResultSetToCategory(ResultSet rs) throws SQLException {
        return new QuestionCategory(
                rs.getInt("category_id"),
                rs.getString("category_name"),
                rs.getString("description")
        );
    }
}