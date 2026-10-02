package com.nextit.repository;

import com.nextit.config.DatabaseConfig;
import com.nextit.exception.AppException;
import com.nextit.model.StudentSkill;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcStudentSkillRepository {

    public List<StudentSkill> findByStudentId(int studentId) {
        List<StudentSkill> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM student_skills WHERE student_id=?")) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); return list; }
        } catch (SQLException e) { throw new AppException("Failed to load student skills", e); }
    }

    public List<StudentSkill> findAll() {
        List<StudentSkill> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM student_skills")) {
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) { throw new AppException("Failed to load student skills", e); }
    }

    public void save(StudentSkill s) {
        String sql = "INSERT INTO student_skills (student_id, skill_id, proficiency_level, assessment_score, assessed_by, assessed_at, remarks) VALUES (?,?,?,?,?,?,?)";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getStudentId());
            ps.setInt(2, s.getSkillId());
            ps.setString(3, s.getProficiencyLevel());
            if (s.getAssessmentScore() == null) ps.setNull(4, Types.DECIMAL); else ps.setDouble(4, s.getAssessmentScore());
            if (s.getAssessedBy() == null) ps.setNull(5, Types.INTEGER); else ps.setInt(5, s.getAssessedBy());
            if (s.getAssessedAt() == null) ps.setNull(6, Types.TIMESTAMP); else ps.setTimestamp(6, Timestamp.valueOf(s.getAssessedAt()));
            ps.setString(7, s.getRemarks());
            ps.executeUpdate();
        } catch (SQLException e) { throw new AppException("Failed to save student skill", e); }
    }

    private StudentSkill map(ResultSet rs) throws SQLException {
        StudentSkill s = new StudentSkill();
        s.setStudentSkillId(rs.getInt("student_skill_id"));
        s.setStudentId(rs.getInt("student_id"));
        s.setSkillId(rs.getInt("skill_id"));
        s.setProficiencyLevel(rs.getString("proficiency_level"));
        double score = rs.getDouble("assessment_score");
        s.setAssessmentScore(rs.wasNull() ? null : score);
        int by = rs.getInt("assessed_by");
        s.setAssessedBy(rs.wasNull() ? null : by);
        Timestamp t = rs.getTimestamp("assessed_at");
        s.setAssessedAt(t == null ? null : t.toLocalDateTime());
        s.setRemarks(rs.getString("remarks"));
        return s;
    }
}
