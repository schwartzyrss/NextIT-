package com.nextit.repository;

import com.nextit.config.DatabaseConfig;
import com.nextit.exception.AppException;
import com.nextit.model.JobOpportunity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcJobOpportunityRepository {

    public List<JobOpportunity> findByEmployerId(int employerId) {
        List<JobOpportunity> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM job_opportunities WHERE employer_id=?")) {
            ps.setInt(1, employerId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); return list; }
        } catch (SQLException e) { throw new AppException("Failed to load opportunities", e); }
    }

    public List<JobOpportunity> findAll() {
        List<JobOpportunity> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM job_opportunities")) {
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) { throw new AppException("Failed to load opportunities", e); }
    }

    public List<JobOpportunity> findAvailable() {
        List<JobOpportunity> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM job_opportunities WHERE status='Open'")) {
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) { throw new AppException("Failed to load opportunities", e); }
    }

    public void save(JobOpportunity o) {
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO job_opportunities (employer_id, title, opportunity_type, description, required_skills, location, deadline, status) VALUES (?,?,?,?,?,?,?,?)",
                 Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, o.getEmployerId());
            ps.setString(2, o.getTitle());
            ps.setString(3, o.getOpportunityType());
            ps.setString(4, o.getDescription());
            ps.setString(5, o.getRequiredSkills());
            ps.setString(6, o.getLocation());
            if (o.getDeadline() == null) ps.setNull(7, Types.DATE); else ps.setDate(7, Date.valueOf(o.getDeadline()));
            ps.setString(8, o.getStatus() == null ? "Open" : o.getStatus());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) o.setOpportunityId(k.getInt(1)); }
        } catch (SQLException e) { throw new AppException("Failed to save opportunity", e); }
    }

    public void update(JobOpportunity o) {
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "UPDATE job_opportunities SET title=?, opportunity_type=?, description=?, required_skills=?, location=?, deadline=?, status=? WHERE opportunity_id=?")) {
            ps.setString(1, o.getTitle());
            ps.setString(2, o.getOpportunityType());
            ps.setString(3, o.getDescription());
            ps.setString(4, o.getRequiredSkills());
            ps.setString(5, o.getLocation());
            if (o.getDeadline() == null) ps.setNull(6, Types.DATE); else ps.setDate(6, Date.valueOf(o.getDeadline()));
            ps.setString(7, o.getStatus());
            ps.setInt(8, o.getOpportunityId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new AppException("Failed to update opportunity", e); }
    }

    public void delete(int id) {
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM job_opportunities WHERE opportunity_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { throw new AppException("Failed to delete opportunity", e); }
    }

    private JobOpportunity map(ResultSet rs) throws SQLException {
        JobOpportunity o = new JobOpportunity();
        o.setOpportunityId(rs.getInt("opportunity_id"));
        o.setEmployerId(rs.getInt("employer_id"));
        o.setTitle(rs.getString("title"));
        o.setOpportunityType(rs.getString("opportunity_type"));
        o.setDescription(rs.getString("description"));
        o.setRequiredSkills(rs.getString("required_skills"));
        o.setLocation(rs.getString("location"));
        Date d = rs.getDate("deadline");
        if (d != null) o.setDeadline(d.toLocalDate());
        o.setStatus(rs.getString("status"));
        Timestamp t = rs.getTimestamp("created_at");
        if (t != null) o.setCreatedAt(t.toLocalDateTime());
        return o;
    }
}
