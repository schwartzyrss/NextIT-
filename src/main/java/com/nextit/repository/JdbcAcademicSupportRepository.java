package com.nextit.repository;

import com.nextit.config.DatabaseConfig;
import com.nextit.exception.AppException;
import com.nextit.model.AcademicSupport;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcAcademicSupportRepository {

    public List<AcademicSupport> findByStudentId(int studentId) {
        List<AcademicSupport> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM academic_support WHERE student_id=?")) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); return list; }
        } catch (SQLException e) { throw new AppException("Failed to load academic support", e); }
    }

    public List<AcademicSupport> findAll() {
        List<AcademicSupport> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM academic_support")) {
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) { throw new AppException("Failed to load academic support", e); }
    }

    public void save(AcademicSupport a) {
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO academic_support (student_id, instructor_id, subject_id, support_type, support_date, reason, action_taken, outcome, remarks) VALUES (?,?,?,?,?,?,?,?,?)",
                 Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getStudentId());
            ps.setInt(2, a.getInstructorId());
            if (a.getSubjectId() == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, a.getSubjectId());
            ps.setString(4, a.getSupportType());
            ps.setDate(5, Date.valueOf(a.getSupportDate()));
            ps.setString(6, a.getReason());
            ps.setString(7, a.getActionTaken());
            ps.setString(8, a.getOutcome());
            ps.setString(9, a.getRemarks());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) a.setSupportId(k.getInt(1)); }
        } catch (SQLException e) { throw new AppException("Failed to save academic support", e); }
    }

    private AcademicSupport map(ResultSet rs) throws SQLException {
        AcademicSupport a = new AcademicSupport();
        a.setSupportId(rs.getInt("support_id"));
        a.setStudentId(rs.getInt("student_id"));
        a.setInstructorId(rs.getInt("instructor_id"));
        int sid = rs.getInt("subject_id");
        a.setSubjectId(rs.wasNull() ? null : sid);
        a.setSupportType(rs.getString("support_type"));
        Date d = rs.getDate("support_date");
        if (d != null) a.setSupportDate(d.toLocalDate());
        a.setReason(rs.getString("reason"));
        a.setActionTaken(rs.getString("action_taken"));
        a.setOutcome(rs.getString("outcome"));
        a.setRemarks(rs.getString("remarks"));
        return a;
    }
}
