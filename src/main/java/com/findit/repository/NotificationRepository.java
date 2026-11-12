package com.findit.repository;

import com.findit.model.Notification;
import com.findit.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NotificationRepository {

    private static final int MAX_LENGTH = 500;

    public void save(Notification n) {
        String sql = "INSERT INTO notifications (user_id, message) VALUES (?, ?)";
        String message = n.getMessage().length() > MAX_LENGTH
                ? n.getMessage().substring(0, MAX_LENGTH)
                : n.getMessage();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, n.getUserId());
            ps.setString(2, message);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not save notification", e);
        }
    }

    public List<Notification> findByUser(int userId) {
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY notification_id";
        List<Notification> list = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Notification(
                            rs.getInt("notification_id"),
                            rs.getInt("user_id"),
                            rs.getString("message"),
                            rs.getBoolean("is_read"),
                            rs.getTimestamp("created_at").toLocalDateTime()));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Could not load notifications", e);
        }
    }

    public void markAllRead(int userId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not update notifications", e);
        }
    }
}