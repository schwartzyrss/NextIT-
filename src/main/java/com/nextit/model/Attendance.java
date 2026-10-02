package com.nextit.model;

public class Attendance {
    private int attendanceId;
    private int enrollmentId;
    private java.time.LocalDate attendanceDate;
    private String status;
    private String remarks;
    private int recordedBy;

    public Attendance() {}

    public int getAttendanceId() { return attendanceId; }
    public void setAttendanceId(int attendanceId) { this.attendanceId = attendanceId; }

    public int getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(int enrollmentId) { this.enrollmentId = enrollmentId; }

    public java.time.LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(java.time.LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public int getRecordedBy() { return recordedBy; }
    public void setRecordedBy(int recordedBy) { this.recordedBy = recordedBy; }
}

