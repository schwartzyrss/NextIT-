package com.nextit.repository;

import com.nextit.config.DatabaseConfig;
import com.nextit.exception.AppException;
import com.nextit.model.Certification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcCertificationRepository {

    public List<Certification> findByStudentId(int studentId) {
        List<Certification> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM certifications WHERE student_id=?")) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); return list; }
        } catch (SQLException e) { throw new AppException("Failed to load certifications", e); }
    }

    public void save(Certification c0) {
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO certifications (student_id, certification_name, issuing_organization, issue_date, expiration_date, credential_url) VALUES (?,?,?,?,?,?)",
                 Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, c0.getStudentId());
            ps.setString(2, c0.getCertificationName());
            ps.setString(3, c0.getIssuingOrganization());
            if (c0.getIssueDate() == null) ps.setNull(4, Types.DATE); else ps.setDate(4, Date.valueOf(c0.getIssueDate()));
            if (c0.getExpirationDate() == null) ps.setNull(5, Types.DATE); else ps.setDate(5, Date.valueOf(c0.getExpirationDate()));
            ps.setString(6, c0.getCredentialUrl());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) c0.setCertificationId(k.getInt(1)); }
        } catch (SQLException e) { throw new AppException("Failed to save certification", e); }
    }

    public void update(Certification c0) {
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "UPDATE certifications SET certification_name=?, issuing_organization=?, issue_date=?, expiration_date=?, credential_url=? WHERE certification_id=?")) {
            ps.setString(1, c0.getCertificationName());
            ps.setString(2, c0.getIssuingOrganization());
            if (c0.getIssueDate() == null) ps.setNull(3, Types.DATE); else ps.setDate(3, Date.valueOf(c0.getIssueDate()));
            if (c0.getExpirationDate() == null) ps.setNull(4, Types.DATE); else ps.setDate(4, Date.valueOf(c0.getExpirationDate()));
            ps.setString(5, c0.getCredentialUrl());
            ps.setInt(6, c0.getCertificationId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new AppException("Failed to update certification", e); }
    }

    public void delete(int id) {
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM certifications WHERE certification_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { throw new AppException("Failed to delete certification", e); }
    }

    private Certification map(ResultSet rs) throws SQLException {
        Certification c0 = new Certification();
        c0.setCertificationId(rs.getInt("certification_id"));
        c0.setStudentId(rs.getInt("student_id"));
        c0.setCertificationName(rs.getString("certification_name"));
        c0.setIssuingOrganization(rs.getString("issuing_organization"));
        Date d = rs.getDate("issue_date");
        if (d != null) c0.setIssueDate(d.toLocalDate());
        d = rs.getDate("expiration_date");
        if (d != null) c0.setExpirationDate(d.toLocalDate());
        c0.setCredentialUrl(rs.getString("credential_url"));
        return c0;
    }
}
