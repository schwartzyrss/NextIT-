package com.nextit.service;

import com.nextit.exception.AppException;
import com.nextit.model.*;
import com.nextit.repository.*;
import com.nextit.util.GradeCalculator;
import com.nextit.util.Validator;

import java.util.List;
import java.util.Set;

public class AcademicService {
    private static final Set<String> ATTENDANCE_STATUSES =
            Set.of("Present", "Absent", "Late", "Excused");
    private static final Set<String> ENROLLMENT_STATUSES =
            Set.of("Enrolled", "Dropped", "Completed");

    private final JdbcGradeRepository gradeRepo = new JdbcGradeRepository();
    private final JdbcAttendanceRepository attendanceRepo = new JdbcAttendanceRepository();
    private final JdbcEnrollmentRepository enrollmentRepo = new JdbcEnrollmentRepository();
    private final JdbcStudentRepository studentRepo = new JdbcStudentRepository();
    private final JdbcSubjectRepository subjectRepo = new JdbcSubjectRepository();
    private final JdbcInstructorRepository instructorRepo = new JdbcInstructorRepository();

    public List<Grade> gradesForStudent(int studentId) { return gradeRepo.findByStudentId(studentId); }
    public List<Grade> allGrades() { return gradeRepo.findAll(); }
    public List<Attendance> attendanceForStudent(int studentId) { return attendanceRepo.findByStudentId(studentId); }
    public List<Attendance> allAttendance() { return attendanceRepo.findAll(); }
    public List<Enrollment> enrollmentsForStudent(int studentId) { return enrollmentRepo.findByStudentId(studentId); }
    public List<Enrollment> allEnrollments() { return enrollmentRepo.findAll(); }

    public int studentIdForUser(int userId) {
        return studentRepo.findByUserId(userId).map(Student::getStudentId)
            .orElseThrow(() -> new AppException("Student profile not found for this user."));
    }

    public Enrollment enrollmentFor(int studentId, int subjectId) {
        return enrollmentRepo.findByStudentAndSubject(studentId, subjectId)
                .orElseThrow(() -> new AppException("The student is not enrolled in the selected subject."));
    }

    public void saveEnrollment(Enrollment enrollment) {
        validateEnrollment(enrollment, 0);
        enrollmentRepo.save(enrollment);
    }

    public void updateEnrollment(Enrollment enrollment) {
        if (enrollment.getEnrollmentId() <= 0) throw new AppException("Select an enrollment to update.");
        validateEnrollment(enrollment, enrollment.getEnrollmentId());
        enrollmentRepo.update(enrollment);
    }

    public void deleteEnrollment(int enrollmentId) { enrollmentRepo.delete(enrollmentId); }

    private void validateEnrollment(Enrollment e, int currentId) {
        if (studentRepo.findById(e.getStudentId()).isEmpty()) {
            throw new AppException("Select a valid student.");
        }
        if (subjectRepo.findById(e.getSubjectId()).isEmpty()) {
            throw new AppException("Select a valid subject.");
        }
        Validator.notBlank(e.getAcademicYear(), "Academic year");
        Validator.notBlank(e.getSemester(), "Semester");
        if (e.getStatus() == null || !ENROLLMENT_STATUSES.contains(e.getStatus())) {
            throw new AppException("Enrollment status must be Enrolled, Dropped, or Completed.");
        }
        boolean duplicate = enrollmentRepo.findAll().stream()
                .filter(other -> other.getEnrollmentId() != currentId)
                .anyMatch(other -> other.getStudentId() == e.getStudentId()
                        && other.getSubjectId() == e.getSubjectId()
                        && other.getAcademicYear().equalsIgnoreCase(e.getAcademicYear().trim())
                        && other.getSemester().equalsIgnoreCase(e.getSemester().trim()));
        if (duplicate) {
            throw new AppException("That student is already enrolled in this subject for the selected term.");
        }
        e.setAcademicYear(e.getAcademicYear().trim());
        e.setSemester(e.getSemester().trim());
    }

    public double averageForStudent(int studentId) {
        return GradeCalculator.average(gradesForStudent(studentId));
    }

    public double attendanceRateForStudent(int studentId) {
        return GradeCalculator.attendanceRate(attendanceForStudent(studentId));
    }

    public void recordGrade(Grade g) {
        validateGrade(g);
        g.setGrade(GradeCalculator.percentage(g.getScore(), g.getMaxScore()));
        gradeRepo.save(g);
    }

    public void updateGrade(Grade g) {
        if (g.getGradeId() <= 0) throw new AppException("Select a grade record to update.");
        validateGrade(g);
        g.setGrade(GradeCalculator.percentage(g.getScore(), g.getMaxScore()));
        gradeRepo.update(g);
    }

    public void deleteGrade(int gradeId) { gradeRepo.delete(gradeId); }

    private void validateGrade(Grade g) {
        Validator.notBlank(g.getActivityName(), "Activity name");
        Validator.notBlank(g.getActivityType(), "Activity type");
        if (g.getInstructorId() <= 0) throw new AppException("An instructor is required.");
        if (instructorRepo.findById(g.getInstructorId()).isEmpty()) {
            throw new AppException("The recording instructor could not be found.");
        }
        if (enrollmentRepo.findById(g.getEnrollmentId()).isEmpty()) {
            throw new AppException("Select a valid student and subject.");
        }
        Validator.score(g.getScore(), g.getMaxScore());
    }

    public void recordAttendance(Attendance a) {
        validateAttendance(a);
        attendanceRepo.save(a);
    }

    public void updateAttendance(Attendance a) {
        if (a.getAttendanceId() <= 0) throw new AppException("Select an attendance record to update.");
        validateAttendance(a);
        attendanceRepo.update(a);
    }

    public void deleteAttendance(int attendanceId) { attendanceRepo.delete(attendanceId); }

    private void validateAttendance(Attendance a) {
        if (a.getAttendanceDate() == null) throw new AppException("Attendance date is required.");
        if (a.getStatus() == null || !ATTENDANCE_STATUSES.contains(a.getStatus())) {
            throw new AppException("Attendance status must be Present, Absent, Late, or Excused.");
        }
        if (a.getRecordedBy() <= 0) throw new AppException("An instructor is required.");
        if (instructorRepo.findById(a.getRecordedBy()).isEmpty()) {
            throw new AppException("The recording instructor could not be found.");
        }
        if (enrollmentRepo.findById(a.getEnrollmentId()).isEmpty()) {
            throw new AppException("Select a valid student and subject.");
        }
        boolean duplicate = attendanceRepo.findAll().stream()
                .filter(other -> other.getAttendanceId() != a.getAttendanceId())
                .anyMatch(other -> other.getEnrollmentId() == a.getEnrollmentId()
                        && a.getAttendanceDate().equals(other.getAttendanceDate()));
        if (duplicate) {
            throw new AppException("Attendance for that enrollment is already recorded on this date. Edit the existing record instead.");
        }
    }
}
