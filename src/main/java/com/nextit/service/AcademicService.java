package com.nextit.service;

import com.nextit.exception.AppException;
import com.nextit.model.*;
import com.nextit.repository.*;

import java.util.List;

public class AcademicService {
    private final JdbcGradeRepository gradeRepo = new JdbcGradeRepository();
    private final JdbcAttendanceRepository attendanceRepo = new JdbcAttendanceRepository();
    private final JdbcEnrollmentRepository enrollmentRepo = new JdbcEnrollmentRepository();
    private final JdbcStudentRepository studentRepo = new JdbcStudentRepository();

    public List<Grade> gradesForStudent(int studentId) { return gradeRepo.findByStudentId(studentId); }
    public List<Grade> allGrades() { return gradeRepo.findAll(); }
    public List<Attendance> attendanceForStudent(int studentId) { return attendanceRepo.findByStudentId(studentId); }
    public List<Attendance> allAttendance() { return attendanceRepo.findAll(); }
    public List<Enrollment> enrollmentsForStudent(int studentId) { return enrollmentRepo.findByStudentId(studentId); }

    public int studentIdForUser(int userId) {
        return studentRepo.findByUserId(userId).map(Student::getStudentId)
            .orElseThrow(() -> new AppException("Student profile not found for this user."));
    }

    public void recordGrade(Grade g) {
        if (g.getActivityName() == null || g.getActivityName().isBlank()) throw new AppException("Activity name is required.");
        if (g.getScore() < 0 || g.getMaxScore() <= 0 || g.getScore() > g.getMaxScore())
            throw new AppException("Score must be between 0 and maximum score.");
        gradeRepo.save(g);
    }

    public void recordAttendance(Attendance a) {
        if (a.getAttendanceDate() == null) throw new AppException("Attendance date is required.");
        attendanceRepo.save(a);
    }
}
