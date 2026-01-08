package com.university.questionbank.model;

import com.google.gson.annotations.SerializedName;

public class Student {
    @SerializedName("student_id")
    private int studentId;

    @SerializedName("id_number")
    private String idNumber;

    private String name;
    private String gender;

    @SerializedName("class_name")
    private String className;

    private String major;
    private String phone;
    private String email;

    @SerializedName("created_at")
    private String createdAt;

    public Student() {
    }

    public Student(int studentId, String idNumber, String name, String gender, String className, String major, String phone, String email, String createdAt) {
        this.studentId = studentId;
        this.idNumber = idNumber;
        this.name = name;
        this.gender = gender;
        this.className = className;
        this.major = major;
        this.phone = phone;
        this.email = email;
        this.createdAt = createdAt;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    // 添加 getId() 方法用于 Supabase REST API
    public int getId() {
        return studentId;
    }

    public void setId(int id) {
        this.studentId = id;
    }

    @Override
    public String toString() {
        return "Student{" +
                "studentId=" + studentId +
                ", idNumber='" + idNumber + '\'' +
                ", name='" + name + '\'' +
                ", gender='" + gender + '\'' +
                ", className='" + className + '\'' +
                ", major='" + major + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}
