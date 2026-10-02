package com.nextit.repository;

import com.nextit.config.DatabaseConfig;
import com.nextit.exception.AppException;
import com.nextit.model.ProjectEvaluation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcProjectEvaluationRepository {

    public List<ProjectEvaluation> findByStudentId(int studentId) {
        List<ProjectEvaluation> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM project_evaluations WHERE student_id=?")) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); return list; }
        } catch (SQLException e) { throw new AppException("Failed to load evaluations", e); }
    }

    public List<ProjectEvaluation> findAll() {
        List<ProjectEvaluation> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM project_evaluations")) {
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) { throw new AppException("Failed to load evaluations", e); }
    }

    public void save(ProjectEvaluation e0) {
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO project_evaluations (project_id, student_id, instructor_id, contribution_score, technical_score, teamwork_score, overall_score, feedback) VALUES (?,?,?,?,?,?,?,?)",
                 Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, e0.getProjectId());
            ps.setInt(2, e0.getStudentId());
            ps.setInt(3, e0.getInstructorId());
            setNullable(ps, 4, e0.getContributionScore());
            setNullable(ps, 5, e0.getTechnicalScore());
            setNullable(ps, 6, e0.getTeamworkScore());
            setNullable(ps, 7, e0.getOverallScore());
            ps.setString(8, e0.getFeedback());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) e0.setEvaluationId(k.getInt(1)); }
        } catch (SQLException e) { throw new AppException("Failed to save evaluation", e); }
    }

    private void setNullable(PreparedStatement ps, int i, Double v) throws SQLException {
        if (v == null) ps.setNull(i, Types.DECIMAL); else ps.setDouble(i, v);
    }

    private ProjectEvaluation map(ResultSet rs) throws SQLException {
        ProjectEvaluation e0 = new ProjectEvaluation();
        e0.setEvaluationId(rs.getInt("evaluation_id"));
        e0.setProjectId(rs.getInt("project_id"));
        e0.setStudentId(rs.getInt("student_id"));
        e0.setInstructorId(rs.getInt("instructor_id"));
        setNullableGet(rs, "contribution_score", e0::setContributionScore);
        setNullableGet(rs, "technical_score", e0::setTechnicalScore);
        setNullableGet(rs, "teamwork_score", e0::setTeamworkScore);
        setNullableGet(rs, "overall_score", e0::setOverallScore);
        e0.setFeedback(rs.getString("feedback"));
        Timestamp t = rs.getTimestamp("evaluated_at");
        if (t != null) e0.setEvaluatedAt(t.toLocalDateTime());
        return e0;
    }

    private interface DoubleSetter { void set(Double v); }
    private void setNullableGet(ResultSet rs, String col, DoubleSetter setter) throws SQLException {
        double v = rs.getDouble(col);
        setter.set(rs.wasNull() ? null : v);
    }
}
