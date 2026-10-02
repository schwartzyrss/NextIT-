package com.nextit.repository;

import com.nextit.config.DatabaseConfig;
import com.nextit.exception.AppException;
import com.nextit.model.TechnicalSkill;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcTechnicalSkillRepository {

    public List<TechnicalSkill> findAll() {
        List<TechnicalSkill> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM technical_skills ORDER BY skill_name")) {
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) { throw new AppException("Failed to load skills", e); }
    }

    public void save(TechnicalSkill s) {
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT INTO technical_skills (skill_name, category, description) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getSkillName());
            ps.setString(2, s.getCategory());
            ps.setString(3, s.getDescription());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) s.setSkillId(k.getInt(1)); }
        } catch (SQLException e) { throw new AppException("Failed to save skill", e); }
    }

    public void update(TechnicalSkill s) {
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE technical_skills SET skill_name=?, category=?, description=? WHERE skill_id=?")) {
            ps.setString(1, s.getSkillName());
            ps.setString(2, s.getCategory());
            ps.setString(3, s.getDescription());
            ps.setInt(4, s.getSkillId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new AppException("Failed to update skill", e); }
    }

    public void delete(int id) {
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM technical_skills WHERE skill_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { throw new AppException("Failed to delete skill", e); }
    }

    private TechnicalSkill map(ResultSet rs) throws SQLException {
        TechnicalSkill s = new TechnicalSkill();
        s.setSkillId(rs.getInt("skill_id"));
        s.setSkillName(rs.getString("skill_name"));
        s.setCategory(rs.getString("category"));
        s.setDescription(rs.getString("description"));
        return s;
    }
}
