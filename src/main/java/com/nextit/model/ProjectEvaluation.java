package com.nextit.model;

public class ProjectEvaluation {
    private int evaluationId;
    private int projectId;
    private int studentId;
    private int instructorId;
    private Double contributionScore;
    private Double technicalScore;
    private Double teamworkScore;
    private Double overallScore;
    private String feedback;
    private java.time.LocalDateTime evaluatedAt;

    public ProjectEvaluation() {}

    public int getEvaluationId() { return evaluationId; }
    public void setEvaluationId(int evaluationId) { this.evaluationId = evaluationId; }

    public int getProjectId() { return projectId; }
    public void setProjectId(int projectId) { this.projectId = projectId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getInstructorId() { return instructorId; }
    public void setInstructorId(int instructorId) { this.instructorId = instructorId; }

    public Double getContributionScore() { return contributionScore; }
    public void setContributionScore(Double contributionScore) { this.contributionScore = contributionScore; }

    public Double getTechnicalScore() { return technicalScore; }
    public void setTechnicalScore(Double technicalScore) { this.technicalScore = technicalScore; }

    public Double getTeamworkScore() { return teamworkScore; }
    public void setTeamworkScore(Double teamworkScore) { this.teamworkScore = teamworkScore; }

    public Double getOverallScore() { return overallScore; }
    public void setOverallScore(Double overallScore) { this.overallScore = overallScore; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public java.time.LocalDateTime getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(java.time.LocalDateTime evaluatedAt) { this.evaluatedAt = evaluatedAt; }
}

