package com.nextit.repository;

import com.nextit.config.DatabaseConfig;
import com.nextit.exception.AppException;
import com.nextit.model.PortfolioAccess;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcPortfolioAccessRepository {

    public List<PortfolioAccess> findByPortfolioId(int portfolioId) {
        List<PortfolioAccess> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM portfolio_access WHERE portfolio_id=?")) {
            ps.setInt(1, portfolioId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); return list; }
        } catch (SQLException e) { throw new AppException("Failed to load portfolio access", e); }
    }

    public void save(PortfolioAccess a) {
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO portfolio_access (portfolio_id, employer_id, access_status, granted_at) VALUES (?,?,?,?)",
                 Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getPortfolioId());
            ps.setInt(2, a.getEmployerId());
            ps.setString(3, a.getAccessStatus());
            if (a.getGrantedAt() == null) ps.setNull(4, Types.TIMESTAMP); else ps.setTimestamp(4, Timestamp.valueOf(a.getGrantedAt()));
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) a.setAccessId(k.getInt(1)); }
        } catch (SQLException e) { throw new AppException("Failed to save portfolio access", e); }
    }

    private PortfolioAccess map(ResultSet rs) throws SQLException {
        PortfolioAccess a = new PortfolioAccess();
        a.setAccessId(rs.getInt("access_id"));
        a.setPortfolioId(rs.getInt("portfolio_id"));
        a.setEmployerId(rs.getInt("employer_id"));
        a.setAccessStatus(rs.getString("access_status"));
        Timestamp t = rs.getTimestamp("granted_at");
        if (t != null) a.setGrantedAt(t.toLocalDateTime());
        return a;
    }
}
