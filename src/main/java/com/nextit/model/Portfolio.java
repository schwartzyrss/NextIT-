package com.nextit.model;

public class Portfolio {
    private int portfolioId;
    private int studentId;
    private String headline;
    private String bio;
    private String careerGoal;
    private String visibility;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

    public Portfolio() {}

    public int getPortfolioId() { return portfolioId; }
    public void setPortfolioId(int portfolioId) { this.portfolioId = portfolioId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getHeadline() { return headline; }
    public void setHeadline(String headline) { this.headline = headline; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getCareerGoal() { return careerGoal; }
    public void setCareerGoal(String careerGoal) { this.careerGoal = careerGoal; }

    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }

    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

