package com.nextit.repository;

import com.nextit.config.DatabaseConfig;
import com.nextit.exception.AppException;
import com.nextit.model.Enrollment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcEnrollmentRepository {

    public List<Enrollment> findByStudentId(int studentId) {
        List<Enrollment> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM enrollments WHERE student_id=?")) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Enrollment e = new Enrollment();
                    e.setEnrollmentId(rs.getInt("enrollment_id"));
                    e.setStudentId(rs.getInt("student_id"));
                    e.setSubjectId(rs.getInt("subject_id"));
                    e.setAcademicYear(rs.getString("academic_year"));
                    e.setSemester(rs.getString("semester"));
                    e.setStatus(rs.getString("status"));
                    list.add(e);
                }
                return list;
            }
        } catch (SQLException e) { throw new AppException("Failed to load enrollments", e); }
    }

    public List<Enrollment> findAll() {
        List<Enrollment> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM enrollments")) {
            while (rs.next()) {
                Enrollment e = new Enrollment();
                e.setEnrollmentId(rs.getInt("enrollment_id"));
                e.setStudentId(rs.getInt("student_id"));
                e.setSubjectId(rs.getInt("subject_id"));
                e.setAcademicYear(rs.getString("academic_year"));
                e.setSemester(rs.getString("semester"));
                e.setStatus(rs.getString("status"));
                list.add(e);
            }
            return list;
        } catch (SQLException e) { throw new AppException("Failed to load enrollments", e); }
    }
}
