package com.nextit.model;

public class AcademicSupport {
    private int supportId;
    private int studentId;
    private int instructorId;
    private Integer subjectId;
    private String supportType;
    private java.time.LocalDate supportDate;
    private String reason;
    private String actionTaken;
    private String outcome;
    private String remarks;

    public AcademicSupport() {}

    public int getSupportId() { return supportId; }
    public void setSupportId(int supportId) { this.supportId = supportId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getInstructorId() { return instructorId; }
    public void setInstructorId(int instructorId) { this.instructorId = instructorId; }

    public Integer getSubjectId() { return subjectId; }
    public void setSubjectId(Integer subjectId) { this.subjectId = subjectId; }

    public String getSupportType() { return supportType; }
    public void setSupportType(String supportType) { this.supportType = supportType; }

    public java.time.LocalDate getSupportDate() { return supportDate; }
    public void setSupportDate(java.time.LocalDate supportDate) { this.supportDate = supportDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getActionTaken() { return actionTaken; }
    public void setActionTaken(String actionTaken) { this.actionTaken = actionTaken; }

    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}

