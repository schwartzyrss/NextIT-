package com.nextit.model;

public class Grade {
    private int gradeId;
    private int enrollmentId;
    private int instructorId;
    private String activityName;
    private String activityType;
    private double score;
    private double maxScore;
    private Double grade;
    private String remarks;
    private java.time.LocalDateTime recordedAt;

    public Grade() {}

    public int getGradeId() { return gradeId; }
    public void setGradeId(int gradeId) { this.gradeId = gradeId; }

    public int getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(int enrollmentId) { this.enrollmentId = enrollmentId; }

    public int getInstructorId() { return instructorId; }
    public void setInstructorId(int instructorId) { this.instructorId = instructorId; }

    public String getActivityName() { return activityName; }
    public void setActivityName(String activityName) { this.activityName = activityName; }

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public double getMaxScore() { return maxScore; }
    public void setMaxScore(double maxScore) { this.maxScore = maxScore; }

    public Double getGrade() { return grade; }
    public void setGrade(Double grade) { this.grade = grade; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public java.time.LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(java.time.LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}

