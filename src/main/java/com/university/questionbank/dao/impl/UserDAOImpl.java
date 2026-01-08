package com.university.questionbank.dao.impl;

import com.university.questionbank.dao.UserDAO;
import com.university.questionbank.model.Role;
import com.university.questionbank.model.User;
import com.university.questionbank.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl implements UserDAO {

    @Override
    public User getUserByUsername(String username) throws SQLException {
        // 先单独查询用户表
        String sql = "" +
                "SELECT u.* " +
                "FROM users u " +
                "WHERE u.username = ?;";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // 查询角色信息
                String roleSql = "" +
                        "SELECT r.* " +
                        "FROM roles r " +
                        "WHERE r.role_id = ?;";
                
                try (PreparedStatement rolePstmt = conn.prepareStatement(roleSql)) {
                    rolePstmt.setInt(1, rs.getInt("role_id"));
                    ResultSet roleRs = rolePstmt.executeQuery();
                    
                    if (roleRs.next()) {
                        Role role = new Role(
                                roleRs.getInt("role_id"),
                                roleRs.getString("role_name"),
                                roleRs.getString("description")
                        );
                        
                        return new User(
                                rs.getInt("user_id"),
                                rs.getString("username"),
                                rs.getString("password"),
                                rs.getString("real_name"),
                                rs.getString("email"),
                                rs.getString("phone"),
                                role,
                                rs.getInt("status"),
                                rs.getString("created_at")
                        );
                    }
                }
            }
        }
        return null;
    }

    @Override
    public User getUserById(int userId) throws SQLException {
        String sql = "" +
                "SELECT u.*, r.role_id, r.role_name, r.description " +
                "FROM users u " +
                "JOIN roles r ON u.role_id = r.role_id " +
                "WHERE u.user_id = ?;";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        }
        return null;
    }

    @Override
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "" +
                "SELECT u.*, r.role_id, r.role_name, r.description " +
                "FROM users u " +
                "JOIN roles r ON u.role_id = r.role_id " +
                "ORDER BY u.user_id;";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }

    @Override
    public void addUser(User user) throws SQLException {
        String sql = "" +
                "INSERT INTO users (username, password, real_name, email, phone, role_id, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?);";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getPhone());
            pstmt.setInt(6, user.getRole().getRoleId());
            pstmt.setInt(7, user.getStatus());

            pstmt.executeUpdate();
        }
    }

    @Override
    public void updateUser(User user) throws SQLException {
        String sql = "" +
                "UPDATE users SET " +
                "  username = ?, " +
                "  password = ?, " +
                "  real_name = ?, " +
                "  email = ?, " +
                "  phone = ?, " +
                "  role_id = ?, " +
                "  status = ? " +
                "WHERE user_id = ?;";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getPhone());
            pstmt.setInt(6, user.getRole().getRoleId());
            pstmt.setInt(7, user.getStatus());
            pstmt.setInt(8, user.getUserId());

            pstmt.executeUpdate();
        }
    }

    @Override
    public void deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?;";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        }
    }

    @Override
    public void updateUserStatus(int userId, int status) throws SQLException {
        String sql = "UPDATE users SET status = ? WHERE user_id = ?;";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, status);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
    }

    // 将ResultSet映射为User对象
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        Role role = new Role(
                rs.getInt("role_id"),
                rs.getString("role_name"),
                rs.getString("description")
        );

        return new User(
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("real_name"),
                rs.getString("email"),
                rs.getString("phone"),
                role,
                rs.getInt("status"),
                rs.getString("created_at")
        );
    }
}