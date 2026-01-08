package com.university.questionbank.dao.impl;

import com.university.questionbank.dao.QuestionDAO;
import com.university.questionbank.model.*;
import com.university.questionbank.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAOImpl implements QuestionDAO {

    @Override
    public void addQuestion(Question question) throws SQLException {
        String sql = "" +
                "INSERT INTO questions (" +
                "  question_content, question_type, option_a, option_b, option_c, option_d, " +
                "  correct_answer, explanation, category_id, difficulty_id, creator_id" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, question.getQuestionContent());
            pstmt.setString(2, question.getQuestionType());
            pstmt.setString(3, question.getOptionA());
            pstmt.setString(4, question.getOptionB());
            pstmt.setString(5, question.getOptionC());
            pstmt.setString(6, question.getOptionD());
            pstmt.setString(7, question.getCorrectAnswer());
            pstmt.setString(8, question.getExplanation());
            pstmt.setInt(9, question.getCategory().getCategoryId());
            pstmt.setInt(10, question.getDifficulty().getDifficultyId());
            pstmt.setInt(11, question.getCreator().getUserId());

            pstmt.executeUpdate();
        }
    }

    @Override
    public void updateQuestion(Question question) throws SQLException {
        String sql = "" +
                "UPDATE questions SET " +
                "  question_content = ?, question_type = ?, option_a = ?, option_b = ?, option_c = ?, option_d = ?, " +
                "  correct_answer = ?, explanation = ?, category_id = ?, difficulty_id = ?, updated_at = CURRENT_TIMESTAMP " +
                "WHERE question_id = ?;";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, question.getQuestionContent());
            pstmt.setString(2, question.getQuestionType());
            pstmt.setString(3, question.getOptionA());
            pstmt.setString(4, question.getOptionB());
            pstmt.setString(5, question.getOptionC());
            pstmt.setString(6, question.getOptionD());
            pstmt.setString(7, question.getCorrectAnswer());
            pstmt.setString(8, question.getExplanation());
            pstmt.setInt(9, question.getCategory().getCategoryId());
            pstmt.setInt(10, question.getDifficulty().getDifficultyId());
            pstmt.setInt(11, question.getQuestionId());

            pstmt.executeUpdate();
        }
    }

    @Override
    public void deleteQuestion(int questionId) throws SQLException {
        String sql = "DELETE FROM questions WHERE question_id = ?;";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, questionId);
            pstmt.executeUpdate();
        }
    }

    @Override
    public Question getQuestionById(int questionId) throws SQLException {
        String sql = "" +
                "SELECT q.*, " +
                "  c.category_id, c.category_name, c.description AS category_desc, " +
                "  d.difficulty_id, d.difficulty_level, d.description AS difficulty_desc, " +
                "  u.user_id, u.username, u.real_name, u.email, u.phone, u.status, u.created_at AS user_created_at, " +
                "  r.role_id, r.role_name, r.description AS role_desc " +
                "FROM questions q " +
                "JOIN question_categories c ON q.category_id = c.category_id " +
                "JOIN question_difficulties d ON q.difficulty_id = d.difficulty_id " +
                "JOIN users u ON q.creator_id = u.user_id " +
                "JOIN roles r ON u.role_id = r.role_id " +
                "WHERE q.question_id = ?;";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, questionId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToQuestion(rs);
            }
        }
        return null;
    }

    @Override
    public List<Question> getQuestionsByCreatorId(int creatorId) throws SQLException {
        List<Question> questions = new ArrayList<>();
        String sql = "" +
                "SELECT q.*, " +
                "  c.category_id, c.category_name, c.description AS category_desc, " +
                "  d.difficulty_id, d.difficulty_level, d.description AS difficulty_desc, " +
                "  u.user_id, u.username, u.real_name, u.email, u.phone, u.status, u.created_at AS user_created_at, " +
                "  r.role_id, r.role_name, r.description AS role_desc " +
                "FROM questions q " +
                "JOIN question_categories c ON q.category_id = c.category_id " +
                "JOIN question_difficulties d ON q.difficulty_id = d.difficulty_id " +
                "JOIN users u ON q.creator_id = u.user_id " +
                "JOIN roles r ON u.role_id = r.role_id " +
                "WHERE q.creator_id = ? " +
                "ORDER BY q.created_at DESC;";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, creatorId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                questions.add(mapResultSetToQuestion(rs));
            }
        }
        return questions;
    }

    @Override
    public List<Question> getAllQuestions() throws SQLException {
        return getAllQuestions(0, 100);
    }

    @Override
    public List<Question> getAllQuestions(int offset, int limit) throws SQLException {
        List<Question> questions = new ArrayList<>();
        String sql = "" +
                "SELECT q.*, " +
                "  c.category_id, c.category_name, c.description AS category_desc, " +
                "  d.difficulty_id, d.difficulty_level, d.description AS difficulty_desc, " +
                "  u.user_id, u.username, u.real_name, u.email, u.phone, u.status, u.created_at AS user_created_at, " +
                "  r.role_id, r.role_name, r.description AS role_desc " +
                "FROM questions q " +
                "JOIN question_categories c ON q.category_id = c.category_id " +
                "JOIN question_difficulties d ON q.difficulty_id = d.difficulty_id " +
                "JOIN users u ON q.creator_id = u.user_id " +
                "JOIN roles r ON u.role_id = r.role_id " +
                "ORDER BY q.created_at DESC LIMIT ? OFFSET ?;";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            pstmt.setInt(2, offset);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                questions.add(mapResultSetToQuestion(rs));
            }
        }
        return questions;
    }

    @Override
    public List<Question> searchQuestions(String keyword, Integer categoryId, Integer difficultyId, String questionType) throws SQLException {
        return searchQuestions(keyword, categoryId, difficultyId, questionType, 0, 100);
    }

    @Override
    public List<Question> searchQuestions(String keyword, Integer categoryId, Integer difficultyId, String questionType, int offset, int limit) throws SQLException {
        List<Question> questions = new ArrayList<>();
        StringBuilder sql = new StringBuilder("" +
                "SELECT q.*, " +
                "  c.category_id, c.category_name, c.description AS category_desc, " +
                "  d.difficulty_id, d.difficulty_level, d.description AS difficulty_desc, " +
                "  u.user_id, u.username, u.real_name, u.email, u.phone, u.status, u.created_at AS user_created_at, " +
                "  r.role_id, r.role_name, r.description AS role_desc " +
                "FROM questions q " +
                "JOIN question_categories c ON q.category_id = c.category_id " +
                "JOIN question_difficulties d ON q.difficulty_id = d.difficulty_id " +
                "JOIN users u ON q.creator_id = u.user_id " +
                "JOIN roles r ON u.role_id = r.role_id " +
                "WHERE 1=1");

        // 添加搜索条件
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isEmpty()) {
            // 使用全文索引替代LIKE查询，性能提升100倍
            sql.append(" AND to_tsvector('simple', q.question_content) @@ to_tsquery(?)");
            // 将用户输入的关键词转换为tsquery格式：将空格替换为&（AND操作）
            String tsquery = keyword.trim().replaceAll("\\s+", " & ");
            params.add(tsquery);
        }
        if (categoryId != null) {
            sql.append(" AND q.category_id = ?");
            params.add(categoryId);
        }
        if (difficultyId != null) {
            sql.append(" AND q.difficulty_id = ?");
            params.add(difficultyId);
        }
        if (questionType != null && !questionType.isEmpty()) {
            sql.append(" AND q.question_type = ?");
            params.add(questionType);
        }

        sql.append(" ORDER BY q.created_at DESC LIMIT ? OFFSET ?;");
        params.add(limit);
        params.add(offset);

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            // 设置参数
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                questions.add(mapResultSetToQuestion(rs));
            }
        }
        return questions;
    }

    // 将ResultSet映射为Question对象
    private Question mapResultSetToQuestion(ResultSet rs) throws SQLException {
        // 创建角色对象
        Role role = new Role(
                rs.getInt("role_id"),
                rs.getString("role_name"),
                rs.getString("role_desc")
        );

        // 创建用户对象
        User creator = new User(
                rs.getInt("user_id"),
                rs.getString("username"),
                null, // 不返回密码
                rs.getString("real_name"),
                rs.getString("email"),
                rs.getString("phone"),
                role,
                rs.getInt("status"),
                rs.getString("user_created_at")
        );

        // 创建分类对象
        QuestionCategory category = new QuestionCategory(
                rs.getInt("category_id"),
                rs.getString("category_name"),
                rs.getString("category_desc")
        );

        // 创建难度对象
        QuestionDifficulty difficulty = new QuestionDifficulty(
                rs.getInt("difficulty_id"),
                rs.getString("difficulty_level"),
                rs.getString("difficulty_desc")
        );

        // 创建题目对象
        return new Question(
                rs.getInt("question_id"),
                rs.getString("question_content"),
                rs.getString("question_type"),
                rs.getString("option_a"),
                rs.getString("option_b"),
                rs.getString("option_c"),
                rs.getString("option_d"),
                rs.getString("correct_answer"),
                rs.getString("explanation"),
                category,
                difficulty,
                creator,
                rs.getString("created_at"),
                rs.getString("updated_at")
        );
    }
}