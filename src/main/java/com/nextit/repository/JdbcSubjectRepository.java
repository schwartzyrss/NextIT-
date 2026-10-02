package com.nextit.repository;

import com.nextit.config.DatabaseConfig;
import com.nextit.exception.AppException;
import com.nextit.model.Subject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcSubjectRepository {

    public List<Subject> findAll() {
        List<Subject> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM subjects ORDER BY subject_code")) {
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) { throw new AppException("Failed to load subjects", e); }
    }

    public void save(Subject s) {
        String sql = "INSERT INTO subjects (subject_code, subject_name, units, description) VALUES (?,?,?,?)";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getSubjectCode());
            ps.setString(2, s.getSubjectName());
            ps.setDouble(3, s.getUnits());
            ps.setString(4, s.getDescription());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) s.setSubjectId(k.getInt(1)); }
        } catch (SQLException e) { throw new AppException("Failed to save subject", e); }
    }

    public void update(Subject s) {
        String sql = "UPDATE subjects SET subject_code=?, subject_name=?, units=?, description=? WHERE subject_id=?";
        try (Connection c = DatabaseConfig.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getSubjectCode());
            ps.setString(2, s.getSubjectName());
            ps.setDouble(3, s.getUnits());
            ps.setString(4, s.getDescription());
            ps.setInt(5, s.getSubjectId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new AppException("Failed to update subject", e); }
    }

    public void delete(int id) {
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM subjects WHERE subject_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { throw new AppException("Failed to delete subject", e); }
    }

    private Subject map(ResultSet rs) throws SQLException {
        Subject s = new Subject();
        s.setSubjectId(rs.getInt("subject_id"));
        s.setSubjectCode(rs.getString("subject_code"));
        s.setSubjectName(rs.getString("subject_name"));
        s.setUnits(rs.getDouble("units"));
        s.setDescription(rs.getString("description"));
        return s;
    }
}
