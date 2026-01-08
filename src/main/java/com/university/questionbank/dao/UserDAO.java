package com.university.questionbank.dao;

import com.university.questionbank.model.User;

import java.sql.SQLException;
import java.util.List;

public interface UserDAO {
    // 根据用户名获取用户
    User getUserByUsername(String username) throws SQLException;

    // 根据用户ID获取用户
    User getUserById(int userId) throws SQLException;

    // 获取所有用户
    List<User> getAllUsers() throws SQLException;

    // 添加用户
    void addUser(User user) throws SQLException;

    // 更新用户
    void updateUser(User user) throws SQLException;

    // 删除用户
    void deleteUser(int userId) throws SQLException;

    // 启用/禁用用户
    void updateUserStatus(int userId, int status) throws SQLException;
}