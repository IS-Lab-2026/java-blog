package com.blog.controller;

import com.blog.util.AppSecurity;
import com.blog.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/notifications")
public class NotificationServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!AppSecurity.requireRole(request, response, "user", "editor", "admin")) {
            return;
        }
        int userId = AppSecurity.userId(request);
        String sql = "SELECT n.notification_id, n.title, n.content, n.link, n.is_read, n.created_at, "
                + "u.user_name AS actor_name FROM notification n LEFT JOIN users u ON u.user_id = n.actor_id "
                + "WHERE n.user_id = ? ORDER BY n.created_at DESC LIMIT 200";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            List<Map<String, Object>> notifications = new ArrayList<>();
            try (ResultSet result = statement.executeQuery()) {
                int columns = result.getMetaData().getColumnCount();
                while (result.next()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    for (int column = 1; column <= columns; column++) {
                        item.put(result.getMetaData().getColumnLabel(column), result.getObject(column));
                    }
                    notifications.add(item);
                }
            }
            request.setAttribute("notifications", notifications);
            request.setAttribute("csrf", AppSecurity.csrfToken(request.getSession(true)));
            request.getRequestDispatcher("/WEB-INF/views/notifications.jsp").forward(request, response);
        } catch (Exception exception) {
            getServletContext().log("Could not load notification inbox", exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Notifications are unavailable.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!AppSecurity.requireRole(request, response, "user", "editor", "admin")) {
            return;
        }
        if (!AppSecurity.validCsrf(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "UPDATE notification SET is_read = TRUE WHERE user_id = ?")) {
            statement.setInt(1, AppSecurity.userId(request));
            statement.executeUpdate();
            response.sendRedirect(request.getContextPath() + "/notifications?read=1");
        } catch (Exception exception) {
            getServletContext().log("Could not mark notifications read", exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }
}
