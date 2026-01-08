package com.university.questionbank.model;

import com.google.gson.annotations.SerializedName;

public class QuestionDifficulty {
    @SerializedName("difficulty_id")
    private int difficultyId;

    @SerializedName("difficulty_level")
    private String difficultyLevel;

    private String description;

    public QuestionDifficulty() {
    }

    public QuestionDifficulty(int difficultyId, String difficultyLevel, String description) {
        this.difficultyId = difficultyId;
        this.difficultyLevel = difficultyLevel;
        this.description = description;
    }

    public int getDifficultyId() {
        return difficultyId;
    }

    public void setDifficultyId(int difficultyId) {
        this.difficultyId = difficultyId;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "QuestionDifficulty{" +
                "difficultyId=" + difficultyId +
                ", difficultyLevel='" + difficultyLevel + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}