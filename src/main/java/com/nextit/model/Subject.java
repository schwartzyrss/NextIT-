package com.nextit.model;

public class Subject {
    private int subjectId;
    private String subjectCode;
    private String subjectName;
    private double units;
    private String description;

    public Subject() {}

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public double getUnits() { return units; }
    public void setUnits(double units) { this.units = units; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

