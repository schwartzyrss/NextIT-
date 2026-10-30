package com.nextit.repository;

import com.nextit.config.DatabaseConfig;
import com.nextit.exception.AppException;
import com.nextit.model.Instructor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcInstructorRepository {

    public Optional<Instructor> findById(int instructorId) {
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM instructors WHERE instructor_id=?")) {
            ps.setInt(1, instructorId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) { throw new AppException("Failed to load instructor", e); }
    }

    public Optional<Instructor> findByUserId(int userId) {
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM instructors WHERE user_id=?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) { throw new AppException("Failed to load instructor", e); }
    }

    public List<Instructor> findAll() {
        List<Instructor> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM instructors")) {
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) { throw new AppException("Failed to load instructors", e); }
    }

    public void save(Instructor i) {
        String sql = "INSERT INTO instructors (user_id, employee_number, department, specialization) VALUES (?,?,?,?)";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, i.getUserId());
            ps.setString(2, i.getEmployeeNumber());
            ps.setString(3, i.getDepartment());
            ps.setString(4, i.getSpecialization());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) i.setInstructorId(k.getInt(1)); }
        } catch (SQLException e) { throw new AppException("Failed to save instructor", e); }
    }

    public void update(Instructor i) {
        String sql = "UPDATE instructors SET employee_number=?, department=?, specialization=? WHERE instructor_id=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, i.getEmployeeNumber());
            ps.setString(2, i.getDepartment());
            ps.setString(3, i.getSpecialization());
            ps.setInt(4, i.getInstructorId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new AppException("Failed to update instructor", e); }
    }

    private Instructor map(ResultSet rs) throws SQLException {
        Instructor i = new Instructor();
        i.setInstructorId(rs.getInt("instructor_id"));
        i.setUserId(rs.getInt("user_id"));
        i.setEmployeeNumber(rs.getString("employee_number"));
        i.setDepartment(rs.getString("department"));
        i.setSpecialization(rs.getString("specialization"));
        return i;
    }
}
