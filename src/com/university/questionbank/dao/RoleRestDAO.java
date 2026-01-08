package com.university.questionbank.dao;

import com.university.questionbank.model.Role;
import com.university.questionbank.util.SupabaseRestAPI;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.logging.Logger;

/**
 * 基于 REST API 的角色数据访问对象
 */
public class RoleRestDAO implements RoleDAO {
    private static final Logger logger = Logger.getLogger(RoleRestDAO.class.getName());
    private static final String TABLE = "/roles";
    private final Gson gson = new Gson();

    @Override
    public Role getRoleById(int roleId) {
        try {
            String query = "role_id=eq." + roleId + "&select=*";
            String response = SupabaseRestAPI.get(TABLE, query);

            if (response == null || response.equals("[]")) {
                logger.warning("角色不存在: " + roleId);
                return null;
            }

            Type listType = new TypeToken<List<Role>>(){}.getType();
            List<Role> roles = gson.fromJson(response, listType);

            return roles.isEmpty() ? null : roles.get(0);
        } catch (Exception e) {
            logger.severe("获取角色失败: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Role getRoleByName(String roleName) {
        try {
            String query = "role_name=eq." + roleName + "&select=*";
            String response = SupabaseRestAPI.get(TABLE, query);

            if (response == null || response.equals("[]")) {
                return null;
            }

            Type listType = new TypeToken<List<Role>>(){}.getType();
            List<Role> roles = gson.fromJson(response, listType);

            return roles.isEmpty() ? null : roles.get(0);
        } catch (Exception e) {
            logger.severe("获取角色失败: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Role> getAllRoles() {
        try {
            String query = "select=*&order=role_id";
            String response = SupabaseRestAPI.get(TABLE, query);

            if (response == null || response.equals("[]")) {
                return List.of();
            }

            Type listType = new TypeToken<List<Role>>(){}.getType();
            return gson.fromJson(response, listType);
        } catch (Exception e) {
            logger.severe("获取角色列表失败: " + e.getMessage());
            return List.of();
        }
    }

    @Override
    public void addRole(Role role) {
        try {
            String json = gson.toJson(role);
            String response = SupabaseRestAPI.post(TABLE, json);
            if (response == null || response.isEmpty()) {
                throw new RuntimeException("添加角色失败");
            }
        } catch (Exception e) {
            logger.severe("添加角色失败: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateRole(Role role) {
        try {
            String query = "role_id=eq." + role.getRoleId();
            String json = gson.toJson(role);
            String response = SupabaseRestAPI.patch(TABLE + "?" + query, json);
            if (response == null || response.isEmpty()) {
                throw new RuntimeException("更新角色失败");
            }
        } catch (Exception e) {
            logger.severe("更新角色失败: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteRole(int roleId) {
        try {
            String query = "role_id=eq." + roleId;
            String response = SupabaseRestAPI.delete(TABLE + "?" + query);
            if (response == null) {
                throw new RuntimeException("删除角色失败");
            }
        } catch (Exception e) {
            logger.severe("删除角色失败: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
