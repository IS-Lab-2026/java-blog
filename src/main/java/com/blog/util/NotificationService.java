package com.blog.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class NotificationService {
    private NotificationService() {
    }

    public static void notifyUser(Connection connection, int recipientId, Integer actorId, String title,
            String content, String link) throws SQLException {
        String sql = "INSERT INTO notification (user_id, actor_id, title, content, link) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, recipientId);
            if (actorId == null) {
                statement.setNull(2, java.sql.Types.INTEGER);
            } else {
                statement.setInt(2, actorId);
            }
            statement.setString(3, title);
            statement.setString(4, content);
            statement.setString(5, link);
            statement.executeUpdate();
        }
    }

    public static void notifyAdmins(Connection connection, Integer actorId, String title, String content,
            String link) throws SQLException {
        String sql = "INSERT INTO notification (user_id, actor_id, title, content, link) "
                + "SELECT user_id, ?, ?, ?, ? FROM users WHERE user_role = 'admin' AND is_active = TRUE "
                + "AND user_id <> ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (actorId == null) {
                statement.setNull(1, java.sql.Types.INTEGER);
            } else {
                statement.setInt(1, actorId);
            }
            statement.setString(2, title);
            statement.setString(3, content);
            statement.setString(4, link);
            statement.setInt(5, actorId == null ? 0 : actorId);
            statement.executeUpdate();
        }
    }

    public static void notifySubscribers(Connection connection, int editorId, Integer actorId, String title,
            String content, String link) throws SQLException {
        String sql = "INSERT INTO notification (user_id, actor_id, title, content, link) "
                + "SELECT s.user_id, ?, ?, ?, ? FROM subscription s "
                + "JOIN users u ON u.user_id = s.user_id AND u.is_active = TRUE "
                + "WHERE s.editor_id = ? AND s.user_id <> ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, actorId == null ? editorId : actorId);
            statement.setString(2, title);
            statement.setString(3, content);
            statement.setString(4, link);
            statement.setInt(5, editorId);
            statement.setInt(6, actorId == null ? editorId : actorId);
            statement.executeUpdate();
        }
    }
}
