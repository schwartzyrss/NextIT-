package com.nextit.repository;

import com.nextit.config.DatabaseConfig;
import com.nextit.exception.AppException;
import com.nextit.model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcStudentRepository {

    public Optional<Student> findByUserId(int userId) {
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM students WHERE user_id=?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) { throw new AppException("Failed to load student", e); }
    }

    public List<Student> findAll() {
        List<Student> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM students")) {
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) { throw new AppException("Failed to load students", e); }
    }

    public void save(Student s) {
        String sql = "INSERT INTO students (user_id, student_number, program, year_level, section, enrollment_status) VALUES (?,?,?,?,?,?)";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getUserId());
            ps.setString(2, s.getStudentNumber());
            ps.setString(3, s.getProgram());
            ps.setInt(4, s.getYearLevel());
            ps.setString(5, s.getSection());
            ps.setString(6, s.getEnrollmentStatus());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) s.setStudentId(k.getInt(1)); }
        } catch (SQLException e) { throw new AppException("Failed to save student", e); }
    }

    private Student map(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setStudentId(rs.getInt("student_id"));
        s.setUserId(rs.getInt("user_id"));
        s.setStudentNumber(rs.getString("student_number"));
        s.setProgram(rs.getString("program"));
        s.setYearLevel(rs.getInt("year_level"));
        s.setSection(rs.getString("section"));
        s.setEnrollmentStatus(rs.getString("enrollment_status"));
        return s;
    }
}
