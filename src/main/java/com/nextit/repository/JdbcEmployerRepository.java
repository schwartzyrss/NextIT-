package com.nextit.repository;

import com.nextit.config.DatabaseConfig;
import com.nextit.exception.AppException;
import com.nextit.model.Employer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcEmployerRepository {

    public Optional<Employer> findById(int employerId) {
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM employers WHERE employer_id=?")) {
            ps.setInt(1, employerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) { throw new AppException("Failed to load employer", e); }
    }

    public Optional<Employer> findByUserId(int userId) {
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM employers WHERE user_id=?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) { throw new AppException("Failed to load employer", e); }
    }

    public List<Employer> findAll() {
        List<Employer> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM employers")) {
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) { throw new AppException("Failed to load employers", e); }
    }

    public void save(Employer e0) {
        String sql = "INSERT INTO employers (user_id, company_name, industry, contact_person, contact_number, address) VALUES (?,?,?,?,?,?)";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, e0.getUserId());
            ps.setString(2, e0.getCompanyName());
            ps.setString(3, e0.getIndustry());
            ps.setString(4, e0.getContactPerson());
            ps.setString(5, e0.getContactNumber());
            ps.setString(6, e0.getAddress());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) e0.setEmployerId(k.getInt(1)); }
        } catch (SQLException e) { throw new AppException("Failed to save employer", e); }
    }

    public void update(Employer e0) {
        String sql = "UPDATE employers SET company_name=?, industry=?, contact_person=?, contact_number=?, address=? WHERE employer_id=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, e0.getCompanyName());
            ps.setString(2, e0.getIndustry());
            ps.setString(3, e0.getContactPerson());
            ps.setString(4, e0.getContactNumber());
            ps.setString(5, e0.getAddress());
            ps.setInt(6, e0.getEmployerId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new AppException("Failed to update employer", e); }
    }

    private Employer map(ResultSet rs) throws SQLException {
        Employer e0 = new Employer();
        e0.setEmployerId(rs.getInt("employer_id"));
        e0.setUserId(rs.getInt("user_id"));
        e0.setCompanyName(rs.getString("company_name"));
        e0.setIndustry(rs.getString("industry"));
        e0.setContactPerson(rs.getString("contact_person"));
        e0.setContactNumber(rs.getString("contact_number"));
        e0.setAddress(rs.getString("address"));
        return e0;
    }
}
