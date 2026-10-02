package com.nextit.repository;

import com.nextit.config.DatabaseConfig;
import com.nextit.exception.AppException;
import com.nextit.model.Portfolio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcPortfolioRepository {

    public Optional<Portfolio> findByStudentId(int studentId) {
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM portfolios WHERE student_id=?")) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(map(rs)) : Optional.empty(); }
        } catch (SQLException e) { throw new AppException("Failed to load portfolio", e); }
    }

    public List<Portfolio> findApproved() {
        List<Portfolio> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM portfolios WHERE visibility='Approved'")) {
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) { throw new AppException("Failed to load portfolios", e); }
    }

    public void save(Portfolio p) {
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO portfolios (student_id, headline, bio, career_goal, visibility) VALUES (?,?,?,?,?)",
                 Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getStudentId());
            ps.setString(2, p.getHeadline());
            ps.setString(3, p.getBio());
            ps.setString(4, p.getCareerGoal());
            ps.setString(5, p.getVisibility() == null ? "Private" : p.getVisibility());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) p.setPortfolioId(k.getInt(1)); }
        } catch (SQLException e) { throw new AppException("Failed to save portfolio", e); }
    }

    public void update(Portfolio p) {
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "UPDATE portfolios SET headline=?, bio=?, career_goal=?, visibility=? WHERE portfolio_id=?")) {
            ps.setString(1, p.getHeadline());
            ps.setString(2, p.getBio());
            ps.setString(3, p.getCareerGoal());
            ps.setString(4, p.getVisibility());
            ps.setInt(5, p.getPortfolioId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new AppException("Failed to update portfolio", e); }
    }

    private Portfolio map(ResultSet rs) throws SQLException {
        Portfolio p = new Portfolio();
        p.setPortfolioId(rs.getInt("portfolio_id"));
        p.setStudentId(rs.getInt("student_id"));
        p.setHeadline(rs.getString("headline"));
        p.setBio(rs.getString("bio"));
        p.setCareerGoal(rs.getString("career_goal"));
        p.setVisibility(rs.getString("visibility"));
        Timestamp t = rs.getTimestamp("created_at");
        if (t != null) p.setCreatedAt(t.toLocalDateTime());
        t = rs.getTimestamp("updated_at");
        if (t != null) p.setUpdatedAt(t.toLocalDateTime());
        return p;
    }
}
