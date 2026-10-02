package com.nextit.repository;

import com.nextit.config.DatabaseConfig;
import com.nextit.exception.AppException;
import com.nextit.model.Project;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcProjectRepository {

    public List<Project> findByStudentId(int studentId) {
        List<Project> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM projects WHERE student_id=?")) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); return list; }
        } catch (SQLException e) { throw new AppException("Failed to load projects", e); }
    }

    public List<Project> findAll() {
        List<Project> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM projects")) {
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) { throw new AppException("Failed to load projects", e); }
    }

    public void save(Project p) {
        String sql = "INSERT INTO projects (student_id, subject_id, project_title, project_type, description, technologies_used, repository_url, demo_url, project_date, status) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setParams(ps, p);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) p.setProjectId(k.getInt(1)); }
        } catch (SQLException e) { throw new AppException("Failed to save project", e); }
    }

    public void update(Project p) {
        String sql = "UPDATE projects SET student_id=?, subject_id=?, project_title=?, project_type=?, description=?, technologies_used=?, repository_url=?, demo_url=?, project_date=?, status=? WHERE project_id=?";
        try (Connection c = DatabaseConfig.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            setParams(ps, p);
            ps.setInt(11, p.getProjectId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new AppException("Failed to update project", e); }
    }

    public void delete(int id) {
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM projects WHERE project_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { throw new AppException("Failed to delete project", e); }
    }

    private void setParams(PreparedStatement ps, Project p) throws SQLException {
        if (p.getStudentId() == null) ps.setNull(1, Types.INTEGER); else ps.setInt(1, p.getStudentId());
        if (p.getSubjectId() == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, p.getSubjectId());
        ps.setString(3, p.getProjectTitle());
        ps.setString(4, p.getProjectType());
        ps.setString(5, p.getDescription());
        ps.setString(6, p.getTechnologiesUsed());
        ps.setString(7, p.getRepositoryUrl());
        ps.setString(8, p.getDemoUrl());
        if (p.getProjectDate() == null) ps.setNull(9, Types.DATE); else ps.setDate(9, Date.valueOf(p.getProjectDate()));
        ps.setString(10, p.getStatus());
    }

    private Project map(ResultSet rs) throws SQLException {
        Project p = new Project();
        p.setProjectId(rs.getInt("project_id"));
        int sid = rs.getInt("student_id"); p.setStudentId(rs.wasNull() ? null : sid);
        int sub = rs.getInt("subject_id"); p.setSubjectId(rs.wasNull() ? null : sub);
        p.setProjectTitle(rs.getString("project_title"));
        p.setProjectType(rs.getString("project_type"));
        p.setDescription(rs.getString("description"));
        p.setTechnologiesUsed(rs.getString("technologies_used"));
        p.setRepositoryUrl(rs.getString("repository_url"));
        p.setDemoUrl(rs.getString("demo_url"));
        Date d = rs.getDate("project_date"); p.setProjectDate(d == null ? null : d.toLocalDate());
        p.setStatus(rs.getString("status"));
        return p;
    }
}
