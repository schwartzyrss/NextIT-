package com.nextit.repository;

import com.nextit.config.DatabaseConfig;
import com.nextit.exception.AppException;
import com.nextit.model.Grade;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcGradeRepository {

    public List<Grade> findByStudentId(int studentId) {
        String sql = "SELECT g.* FROM grades g JOIN enrollments e ON g.enrollment_id=e.enrollment_id WHERE e.student_id=?";
        List<Grade> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); return list; }
        } catch (SQLException e) { throw new AppException("Failed to load grades", e); }
    }

    public List<Grade> findAll() {
        List<Grade> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM grades")) {
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) { throw new AppException("Failed to load grades", e); }
    }

    public void save(Grade g) {
        String sql = "INSERT INTO grades (enrollment_id, instructor_id, activity_name, activity_type, score, max_score, grade, remarks) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, g.getEnrollmentId());
            ps.setInt(2, g.getInstructorId());
            ps.setString(3, g.getActivityName());
            ps.setString(4, g.getActivityType());
            ps.setDouble(5, g.getScore());
            ps.setDouble(6, g.getMaxScore());
            if (g.getGrade() == null) ps.setNull(7, Types.DECIMAL); else ps.setDouble(7, g.getGrade());
            ps.setString(8, g.getRemarks());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) g.setGradeId(k.getInt(1)); }
        } catch (SQLException e) { throw new AppException("Failed to save grade", e); }
    }

    private Grade map(ResultSet rs) throws SQLException {
        Grade g = new Grade();
        g.setGradeId(rs.getInt("grade_id"));
        g.setEnrollmentId(rs.getInt("enrollment_id"));
        g.setInstructorId(rs.getInt("instructor_id"));
        g.setActivityName(rs.getString("activity_name"));
        g.setActivityType(rs.getString("activity_type"));
        g.setScore(rs.getDouble("score"));
        g.setMaxScore(rs.getDouble("max_score"));
        double grade = rs.getDouble("grade");
        g.setGrade(rs.wasNull() ? null : grade);
        g.setRemarks(rs.getString("remarks"));
        Timestamp t = rs.getTimestamp("recorded_at");
        if (t != null) g.setRecordedAt(t.toLocalDateTime());
        return g;
    }
}
