package com.university.questionbank.dao.impl;

import com.university.questionbank.dao.RoleDAO;
import com.university.questionbank.model.Role;
import com.university.questionbank.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RoleDAOImpl implements RoleDAO {

    @Override
    public Role getRoleById(int roleId) throws SQLException {
        String sql = "SELECT * FROM roles WHERE role_id = ?;";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roleId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToRole(rs);
            }
        }
        return null;
    }

    @Override
    public Role getRoleByName(String roleName) throws SQLException {
        String sql = "SELECT * FROM roles WHERE role_name = ?;";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, roleName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToRole(rs);
            }
        }
        return null;
    }

    @Override
    public List<Role> getAllRoles() throws SQLException {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT * FROM roles ORDER BY role_id;";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                roles.add(mapResultSetToRole(rs));
            }
        }
        return roles;
    }

    @Override
    public void addRole(Role role) throws SQLException {
        String sql = "INSERT INTO roles (role_name, description) VALUES (?, ?);";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, role.getRoleName());
            pstmt.setString(2, role.getDescription());
            pstmt.executeUpdate();
        }
    }

    @Override
    public void updateRole(Role role) throws SQLException {
        String sql = "UPDATE roles SET role_name = ?, description = ? WHERE role_id = ?;";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, role.getRoleName());
            pstmt.setString(2, role.getDescription());
            pstmt.setInt(3, role.getRoleId());
            pstmt.executeUpdate();
        }
    }

    @Override
    public void deleteRole(int roleId) throws SQLException {
        String sql = "DELETE FROM roles WHERE role_id = ?;";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roleId);
            pstmt.executeUpdate();
        }
    }

    // 将ResultSet映射为Role对象
    private Role mapResultSetToRole(ResultSet rs) throws SQLException {
        return new Role(
                rs.getInt("role_id"),
                rs.getString("role_name"),
                rs.getString("description")
        );
    }
}