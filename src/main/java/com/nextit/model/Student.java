package com.nextit.model;

public class Student {
    private int studentId;
    private int userId;
    private String studentNumber;
    private String program;
    private int yearLevel;
    private String section;
    private String enrollmentStatus;

    public Student() {}

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getStudentNumber() { return studentNumber; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }

    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }

    public int getYearLevel() { return yearLevel; }
    public void setYearLevel(int yearLevel) { this.yearLevel = yearLevel; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getEnrollmentStatus() { return enrollmentStatus; }
    public void setEnrollmentStatus(String enrollmentStatus) { this.enrollmentStatus = enrollmentStatus; }
}

