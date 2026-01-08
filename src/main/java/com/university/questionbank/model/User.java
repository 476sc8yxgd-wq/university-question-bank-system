package com.university.questionbank.model;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("user_id")
    private Integer userId;  // 改为 Integer，可以是 null

    @SerializedName("username")
    private String username;

    @SerializedName("password")
    private String password;

    @SerializedName("real_name")
    private String realName;

    @SerializedName("email")
    private String email;

    @SerializedName("phone")
    private String phone;

    // role对象不需要序列化，只需要roleId
    private transient Role role;

    @SerializedName("role_id")
    private int roleId;

    @SerializedName("status")
    private int status;

    @SerializedName("created_at")
    private String createdAt;

    public User() {
    }

    public User(Integer userId, String username, String password, String realName, String email, String phone, Role role, int status, String createdAt) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.realName = realName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getFullName() {
        return realName;
    }

    public void setFullName(String fullName) {
        this.realName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    // 添加 getId() 方法用于 Supabase REST API
    public Integer getId() {
        return userId;
    }

    public void setId(Integer id) {
        this.userId = id;
    }

    // 添加 getPasswordHash() 方法用于 Supabase REST API
    public String getPasswordHash() {
        return password;
    }

    public void setPasswordHash(String passwordHash) {
        this.password = passwordHash;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", realName='" + realName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", role=" + role +
                ", roleId=" + roleId +
                ", status=" + status +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}