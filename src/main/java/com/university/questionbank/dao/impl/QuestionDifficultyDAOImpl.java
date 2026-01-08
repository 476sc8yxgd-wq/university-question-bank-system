package com.university.questionbank.dao.impl;

import com.university.questionbank.dao.QuestionDifficultyDAO;
import com.university.questionbank.model.QuestionDifficulty;
import com.university.questionbank.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionDifficultyDAOImpl implements QuestionDifficultyDAO {

    @Override
    public void addDifficulty(QuestionDifficulty difficulty) throws SQLException {
        String sql = "INSERT INTO question_difficulties (difficulty_level, description) VALUES (?, ?);";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, difficulty.getDifficultyLevel());
            pstmt.setString(2, difficulty.getDescription());

            pstmt.executeUpdate();
        }
    }

    @Override
    public void updateDifficulty(QuestionDifficulty difficulty) throws SQLException {
        String sql = "UPDATE question_difficulties SET difficulty_level = ?, description = ? WHERE difficulty_id = ?;";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, difficulty.getDifficultyLevel());
            pstmt.setString(2, difficulty.getDescription());
            pstmt.setInt(3, difficulty.getDifficultyId());

            pstmt.executeUpdate();
        }
    }

    @Override
    public void deleteDifficulty(int difficultyId) throws SQLException {
        String sql = "DELETE FROM question_difficulties WHERE difficulty_id = ?;";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, difficultyId);
            pstmt.executeUpdate();
        }
    }

    @Override
    public QuestionDifficulty getDifficultyById(int difficultyId) throws SQLException {
        String sql = "SELECT difficulty_id, difficulty_level, description FROM question_difficulties WHERE difficulty_id = ?;";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, difficultyId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new QuestionDifficulty(
                        rs.getInt("difficulty_id"),
                        rs.getString("difficulty_level"),
                        rs.getString("description")
                );
            }
        }
        return null;
    }

    @Override
    public List<QuestionDifficulty> getAllDifficulties() throws SQLException {
        List<QuestionDifficulty> difficulties = new ArrayList<>();
        String sql = "SELECT difficulty_id, difficulty_level, description FROM question_difficulties ORDER BY difficulty_id;";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                difficulties.add(new QuestionDifficulty(
                        rs.getInt("difficulty_id"),
                        rs.getString("difficulty_level"),
                        rs.getString("description")
                ));
            }
        }
        return difficulties;
    }
}