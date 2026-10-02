package com.nextit.repository;

import com.nextit.config.DatabaseConfig;
import com.nextit.exception.AppException;
import com.nextit.model.Attendance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcAttendanceRepository {

    public List<Attendance> findByStudentId(int studentId) {
        String sql = "SELECT a.* FROM attendance a JOIN enrollments e ON a.enrollment_id=e.enrollment_id WHERE e.student_id=?";
        List<Attendance> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); return list; }
        } catch (SQLException e) { throw new AppException("Failed to load attendance", e); }
    }

    public List<Attendance> findAll() {
        List<Attendance> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM attendance")) {
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) { throw new AppException("Failed to load attendance", e); }
    }

    public void save(Attendance a) {
        String sql = "INSERT INTO attendance (enrollment_id, attendance_date, status, remarks, recorded_by) VALUES (?,?,?,?,?)";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getEnrollmentId());
            ps.setDate(2, Date.valueOf(a.getAttendanceDate()));
            ps.setString(3, a.getStatus());
            ps.setString(4, a.getRemarks());
            ps.setInt(5, a.getRecordedBy());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) a.setAttendanceId(k.getInt(1)); }
        } catch (SQLException e) { throw new AppException("Failed to save attendance", e); }
    }

    private Attendance map(ResultSet rs) throws SQLException {
        Attendance a = new Attendance();
        a.setAttendanceId(rs.getInt("attendance_id"));
        a.setEnrollmentId(rs.getInt("enrollment_id"));
        Date d = rs.getDate("attendance_date");
        if (d != null) a.setAttendanceDate(d.toLocalDate());
        a.setStatus(rs.getString("status"));
        a.setRemarks(rs.getString("remarks"));
        a.setRecordedBy(rs.getInt("recorded_by"));
        return a;
    }
}
