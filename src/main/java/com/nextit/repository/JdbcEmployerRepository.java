package com.nextit.repository;

import com.nextit.config.DatabaseConfig;
import com.nextit.exception.AppException;
import com.nextit.model.Employer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcEmployerRepository {

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

    private Employer map(ResultSet rs) throws SQLException {
        Employer e = new Employer();
        e.setEmployerId(rs.getInt("employer_id"));
        e.setUserId(rs.getInt("user_id"));
        e.setCompanyName(rs.getString("company_name"));
        e.setIndustry(rs.getString("industry"));
        e.setContactPerson(rs.getString("contact_person"));
        e.setContactNumber(rs.getString("contact_number"));
        e.setAddress(rs.getString("address"));
        return e;
    }
}
