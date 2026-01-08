package com.university.questionbank.dao;

import com.university.questionbank.model.Role;

import java.sql.SQLException;
import java.util.List;

public interface RoleDAO {
    // 根据角色ID获取角色
    Role getRoleById(int roleId) throws SQLException;

    // 根据角色名称获取角色
    Role getRoleByName(String roleName) throws SQLException;

    // 获取所有角色
    List<Role> getAllRoles() throws SQLException;

    // 添加角色
    void addRole(Role role) throws SQLException;

    // 更新角色
    void updateRole(Role role) throws SQLException;

    // 删除角色
    void deleteRole(int roleId) throws SQLException;
}