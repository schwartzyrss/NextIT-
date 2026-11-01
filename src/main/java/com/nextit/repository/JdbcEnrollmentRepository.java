package com.nextit.repository;

import com.nextit.config.DatabaseConfig;
import com.nextit.exception.AppException;
import com.nextit.model.Enrollment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcEnrollmentRepository {

    public List<Enrollment> findByStudentId(int studentId) {
        String sql = "SELECT * FROM enrollments WHERE student_id=?";
        List<Enrollment> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
                return list;
            }
        } catch (SQLException e) { throw new AppException("Failed to load enrollments", e); }
    }

    public Optional<Enrollment> findById(int enrollmentId) {
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM enrollments WHERE enrollment_id=?")) {
            ps.setInt(1, enrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) { throw new AppException("Failed to load enrollment", e); }
    }

    public Optional<Enrollment> findByStudentAndSubject(int studentId, int subjectId) {
        // A student can retake a subject across terms. Prefer the in-progress record,
        // then the most recently created one, so callers get a deterministic result.
        String sql = "SELECT * FROM enrollments WHERE student_id=? AND subject_id=? "
                + "ORDER BY CASE status WHEN 'Enrolled' THEN 0 WHEN 'Completed' THEN 1 ELSE 2 END, "
                + "enrollment_id DESC";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, subjectId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) { throw new AppException("Failed to load enrollment", e); }
    }

    public List<Enrollment> findAll() {
        List<Enrollment> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM enrollments ORDER BY student_id, subject_id")) {
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) { throw new AppException("Failed to load enrollments", e); }
    }

    public void save(Enrollment e) {
        String sql = "INSERT INTO enrollments (student_id, subject_id, academic_year, semester, status) VALUES (?,?,?,?,?)";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, e.getStudentId());
            ps.setInt(2, e.getSubjectId());
            ps.setString(3, e.getAcademicYear());
            ps.setString(4, e.getSemester());
            ps.setString(5, e.getStatus());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) e.setEnrollmentId(k.getInt(1));
            }
        } catch (SQLException ex) { throw new AppException("Failed to save enrollment", ex); }
    }

    public void update(Enrollment e) {
        String sql = "UPDATE enrollments SET student_id=?, subject_id=?, academic_year=?, semester=?, status=? WHERE enrollment_id=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, e.getStudentId());
            ps.setInt(2, e.getSubjectId());
            ps.setString(3, e.getAcademicYear());
            ps.setString(4, e.getSemester());
            ps.setString(5, e.getStatus());
            ps.setInt(6, e.getEnrollmentId());
            ps.executeUpdate();
        } catch (SQLException ex) { throw new AppException("Failed to update enrollment", ex); }
    }

    public void delete(int enrollmentId) {
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM enrollments WHERE enrollment_id=?")) {
            ps.setInt(1, enrollmentId);
            ps.executeUpdate();
        } catch (SQLException ex) { throw new AppException("Failed to delete enrollment", ex); }
    }

    private Enrollment map(ResultSet rs) throws SQLException {
        Enrollment e = new Enrollment();
        e.setEnrollmentId(rs.getInt("enrollment_id"));
        e.setStudentId(rs.getInt("student_id"));
        e.setSubjectId(rs.getInt("subject_id"));
        e.setAcademicYear(rs.getString("academic_year"));
        e.setSemester(rs.getString("semester"));
        e.setStatus(rs.getString("status"));
        return e;
    }
}
