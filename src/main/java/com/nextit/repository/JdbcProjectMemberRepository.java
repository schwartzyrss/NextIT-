package com.nextit.repository;

import com.nextit.config.DatabaseConfig;
import com.nextit.exception.AppException;
import com.nextit.model.ProjectMember;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcProjectMemberRepository {

    public List<ProjectMember> findByProjectId(int projectId) {
        List<ProjectMember> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM project_members WHERE project_id=?")) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); return list; }
        } catch (SQLException e) { throw new AppException("Failed to load project members", e); }
    }

    public void save(ProjectMember m) {
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO project_members (project_id, student_id, role, contribution) VALUES (?,?,?,?)",
                 Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, m.getProjectId());
            ps.setInt(2, m.getStudentId());
            ps.setString(3, m.getRole());
            ps.setString(4, m.getContribution());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) m.setProjectMemberId(k.getInt(1)); }
        } catch (SQLException e) { throw new AppException("Failed to save project member", e); }
    }

    public void delete(int id) {
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM project_members WHERE project_member_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { throw new AppException("Failed to delete project member", e); }
    }

    private ProjectMember map(ResultSet rs) throws SQLException {
        ProjectMember m = new ProjectMember();
        m.setProjectMemberId(rs.getInt("project_member_id"));
        m.setProjectId(rs.getInt("project_id"));
        m.setStudentId(rs.getInt("student_id"));
        m.setRole(rs.getString("role"));
        m.setContribution(rs.getString("contribution"));
        return m;
    }
}
