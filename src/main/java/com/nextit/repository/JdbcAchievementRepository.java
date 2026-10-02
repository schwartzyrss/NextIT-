package com.nextit.repository;

import com.nextit.config.DatabaseConfig;
import com.nextit.exception.AppException;
import com.nextit.model.Achievement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcAchievementRepository {

    public List<Achievement> findByStudentId(int studentId) {
        List<Achievement> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM achievements WHERE student_id=?")) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); return list; }
        } catch (SQLException e) { throw new AppException("Failed to load achievements", e); }
    }

    public void save(Achievement a) {
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO achievements (student_id, title, description, organization, achievement_date, verification_url) VALUES (?,?,?,?,?,?)",
                 Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getStudentId());
            ps.setString(2, a.getTitle());
            ps.setString(3, a.getDescription());
            ps.setString(4, a.getOrganization());
            if (a.getAchievementDate() == null) ps.setNull(5, Types.DATE); else ps.setDate(5, Date.valueOf(a.getAchievementDate()));
            ps.setString(6, a.getVerificationUrl());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) a.setAchievementId(k.getInt(1)); }
        } catch (SQLException e) { throw new AppException("Failed to save achievement", e); }
    }

    public void update(Achievement a) {
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "UPDATE achievements SET title=?, description=?, organization=?, achievement_date=?, verification_url=? WHERE achievement_id=?")) {
            ps.setString(1, a.getTitle());
            ps.setString(2, a.getDescription());
            ps.setString(3, a.getOrganization());
            if (a.getAchievementDate() == null) ps.setNull(4, Types.DATE); else ps.setDate(4, Date.valueOf(a.getAchievementDate()));
            ps.setString(5, a.getVerificationUrl());
            ps.setInt(6, a.getAchievementId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new AppException("Failed to update achievement", e); }
    }

    public void delete(int id) {
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM achievements WHERE achievement_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { throw new AppException("Failed to delete achievement", e); }
    }

    private Achievement map(ResultSet rs) throws SQLException {
        Achievement a = new Achievement();
        a.setAchievementId(rs.getInt("achievement_id"));
        a.setStudentId(rs.getInt("student_id"));
        a.setTitle(rs.getString("title"));
        a.setDescription(rs.getString("description"));
        a.setOrganization(rs.getString("organization"));
        Date d = rs.getDate("achievement_date");
        if (d != null) a.setAchievementDate(d.toLocalDate());
        a.setVerificationUrl(rs.getString("verification_url"));
        return a;
    }
}
