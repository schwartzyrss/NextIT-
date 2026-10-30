package com.nextit.model;

public class StudentSkill {
    private int studentSkillId;
    private int studentId;
    private int skillId;
    private String proficiencyLevel;
    private Double assessmentScore;
    private Integer assessedBy;
    private java.time.LocalDateTime assessedAt;
    private String remarks;

    public StudentSkill() {}

    public int getStudentSkillId() { return studentSkillId; }
    public void setStudentSkillId(int studentSkillId) { this.studentSkillId = studentSkillId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getSkillId() { return skillId; }
    public void setSkillId(int skillId) { this.skillId = skillId; }

    public String getProficiencyLevel() { return proficiencyLevel; }
    public void setProficiencyLevel(String proficiencyLevel) { this.proficiencyLevel = proficiencyLevel; }

    public Double getAssessmentScore() { return assessmentScore; }
    public void setAssessmentScore(Double assessmentScore) { this.assessmentScore = assessmentScore; }

    public Integer getAssessedBy() { return assessedBy; }
    public void setAssessedBy(Integer assessedBy) { this.assessedBy = assessedBy; }

    public java.time.LocalDateTime getAssessedAt() { return assessedAt; }
    public void setAssessedAt(java.time.LocalDateTime assessedAt) { this.assessedAt = assessedAt; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}

