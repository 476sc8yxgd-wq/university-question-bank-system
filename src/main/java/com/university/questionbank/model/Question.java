package com.university.questionbank.model;

import com.google.gson.annotations.SerializedName;

public class Question {
    @SerializedName("question_id")
    private int questionId;

    @SerializedName("question_content")
    private String questionContent;

    @SerializedName("question_type")
    private String questionType;

    @SerializedName("option_a")
    private String optionA;

    @SerializedName("option_b")
    private String optionB;

    @SerializedName("option_c")
    private String optionC;

    @SerializedName("option_d")
    private String optionD;

    @SerializedName("correct_answer")
    private String correctAnswer;

    @SerializedName("explanation")
    private String explanation;

    // 这些对象字段在保存时不需要序列化，只需要ID
    private transient QuestionCategory category;
    private transient QuestionDifficulty difficulty;
    private transient User creator;

    @SerializedName("category_id")
    private int categoryId;

    @SerializedName("difficulty_id")
    private int difficultyId;

    @SerializedName("creator_id")
    private int creatorId;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    public Question() {
    }

    public Question(int questionId, String questionContent, String questionType, String optionA, String optionB, String optionC, String optionD, String correctAnswer, String explanation, QuestionCategory category, QuestionDifficulty difficulty, User creator, String createdAt, String updatedAt) {
        this.questionId = questionId;
        this.questionContent = questionContent;
        this.questionType = questionType;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswer = correctAnswer;
        this.explanation = explanation;
        this.category = category;
        this.difficulty = difficulty;
        this.creator = creator;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getQuestionContent() {
        return questionContent;
    }

    public void setQuestionContent(String questionContent) {
        this.questionContent = questionContent;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getOptionA() {
        return optionA;
    }

    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public QuestionCategory getCategory() {
        return category;
    }

    public void setCategory(QuestionCategory category) {
        this.category = category;
    }

    public QuestionDifficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(QuestionDifficulty difficulty) {
        this.difficulty = difficulty;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getDifficultyId() {
        return difficultyId;
    }

    public void setDifficultyId(int difficultyId) {
        this.difficultyId = difficultyId;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    // 添加 getId() 方法用于 Supabase REST API
    public int getId() {
        return questionId;
    }

    public void setId(int id) {
        this.questionId = id;
    }

    @Override
    public String toString() {
        return "Question{" +
                "questionId=" + questionId +
                ", questionContent='" + questionContent + '\'' +
                ", questionType='" + questionType + '\'' +
                ", optionA='" + optionA + '\'' +
                ", optionB='" + optionB + '\'' +
                ", optionC='" + optionC + '\'' +
                ", optionD='" + optionD + '\'' +
                ", correctAnswer='" + correctAnswer + '\'' +
                ", explanation='" + explanation + '\'' +
                ", category=" + category +
                ", difficulty=" + difficulty +
                ", creator=" + creator +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
}