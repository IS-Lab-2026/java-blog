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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = { "/admin", "/admin/*" })
public class AdminServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!AppSecurity.requireRole(request, response, "admin")) {
            return;
        }
        String tab = request.getParameter("tab");
        request.setAttribute("tab", tab == null ? "overview" : tab);
        request.setAttribute("csrf", AppSecurity.csrfToken(request.getSession(true)));
        try (Connection connection = DBConnection.getConnection()) {
            request.setAttribute("metrics", loadMetrics(connection));
            request.setAttribute("users", loadUsers(connection));
            request.setAttribute("posts", loadPosts(connection));
            request.setAttribute("comments", loadComments(connection));
            request.setAttribute("logins", loadLogins(connection));
            request.setAttribute("messages", loadContactMessages(connection));
            request.getRequestDispatcher("/WEB-INF/views/admin.jsp").forward(request, response);
        } catch (SQLException | ClassNotFoundException exception) {
            getServletContext().log("Could not load admin portal", exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "The admin workspace is unavailable.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!AppSecurity.requireRole(request, response, "admin")) {
            return;
        }
        if (!AppSecurity.validCsrf(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String action = request.getParameter("action");
        int id = parseId(request.getParameter("id"));
        int adminId = AppSecurity.userId(request);
        try (Connection connection = DBConnection.getConnection()) {
            int changed;
            switch (action == null ? "" : action) {
                case "hide-post":
                    changed = update(connection, "UPDATE post SET is_visible = FALSE WHERE blog_id = ?", id);
                    break;
                case "show-post":
                    changed = update(connection, "UPDATE post SET is_visible = TRUE WHERE blog_id = ?", id);
                    break;
                case "hide-comment":
                    changed = update(connection, "UPDATE comment SET is_visible = FALSE WHERE comment_id = ?", id);
                    break;
                case "show-comment":
                    changed = update(connection, "UPDATE comment SET is_visible = TRUE WHERE comment_id = ?", id);
                    break;
                case "suspend-user":
                    changed = updateUser(connection, id, adminId, false);
                    break;
                case "activate-user":
                    changed = updateUser(connection, id, adminId, true);
                    break;
                case "make-editor":
                    changed = updateRole(connection, id, adminId, "editor");
                    break;
                case "make-user":
                    changed = updateRole(connection, id, adminId, "user");
                    break;
                case "resolve-message":
                    changed = update(connection, "UPDATE contact_message SET is_resolved = TRUE WHERE message_id = ?",
                            id);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
            }
            if (changed == 0) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        } catch (SQLException | ClassNotFoundException exception) {
            getServletContext().log("Admin action failed", exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "The requested change could not be saved.");
            return;
        }
        String tab = request.getParameter("tab");
        response.sendRedirect(request.getContextPath() + "/admin?tab="
                + (tab == null ? "overview" : safeTab(tab)) + "&updated=1");
    }

    private Map<String, Integer> loadMetrics(Connection connection) throws SQLException {
        Map<String, Integer> metrics = new LinkedHashMap<>();
        metrics.put("users", count(connection, "SELECT COUNT(*) FROM users"));
        metrics.put("posts", count(connection, "SELECT COUNT(*) FROM post"));
        metrics.put("comments", count(connection, "SELECT COUNT(*) FROM comment"));
        metrics.put("logins", count(connection, "SELECT COUNT(*) FROM login WHERE login_time >= CURRENT_DATE"));
        return metrics;
    }

    private int count(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            return result.next() ? result.getInt(1) : 0;
        }
    }

    private List<Map<String, Object>> loadUsers(Connection connection) throws SQLException {
        return query(connection, "SELECT user_id, user_name, email, user_role, is_active, join_date FROM users "
                + "ORDER BY join_date DESC LIMIT 200");
    }

    private List<Map<String, Object>> loadPosts(Connection connection) throws SQLException {
        return query(connection, "SELECT p.blog_id, p.title, p.slug, p.blog_status, p.is_visible, p.created_at, "
                + "p.views, p.likes, p.comments, u.user_name AS author_name FROM post p "
                + "LEFT JOIN users u ON u.user_id = p.author_id ORDER BY p.created_at DESC LIMIT 200");
    }

    private List<Map<String, Object>> loadComments(Connection connection) throws SQLException {
        return query(connection, "SELECT c.comment_id, c.comment, c.is_visible, c.created_at, u.user_name, "
                + "p.title, p.slug FROM comment c JOIN users u ON u.user_id = c.user_id "
                + "JOIN post p ON p.blog_id = c.blog_id ORDER BY c.created_at DESC LIMIT 200");
    }

    private List<Map<String, Object>> loadLogins(Connection connection) throws SQLException {
        return query(connection, "SELECT l.login_id, l.login_time, l.ip_address, u.user_name, u.email "
                + "FROM login l JOIN users u ON u.user_id = l.user_id ORDER BY l.login_time DESC LIMIT 200");
    }

    private List<Map<String, Object>> loadContactMessages(Connection connection) throws SQLException {
        return query(connection, "SELECT message_id, sender_name, sender_email, subject, message, submitted_at, "
                + "is_resolved FROM contact_message ORDER BY is_resolved, submitted_at DESC LIMIT 200");
    }

    private List<Map<String, Object>> query(Connection connection, String sql) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            int columns = result.getMetaData().getColumnCount();
            while (result.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int column = 1; column <= columns; column++) {
                    row.put(result.getMetaData().getColumnLabel(column), result.getObject(column));
                }
                rows.add(row);
            }
        }
        return rows;
    }

    private int update(Connection connection, String sql, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate();
        }
    }

    private int updateUser(Connection connection, int targetId, int adminId, boolean active) throws SQLException {
        if (targetId == adminId) {
            return 0;
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE users SET is_active = ? WHERE user_id = ? AND user_role <> 'admin'")) {
            statement.setBoolean(1, active);
            statement.setInt(2, targetId);
            return statement.executeUpdate();
        }
    }

    private int updateRole(Connection connection, int targetId, int adminId, String role) throws SQLException {
        if (targetId == adminId) {
            return 0;
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE users SET user_role = ? WHERE user_id = ? AND user_role IN ('user', 'editor')")) {
            statement.setString(1, role);
            statement.setInt(2, targetId);
            return statement.executeUpdate();
        }
    }

    private int parseId(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private String safeTab(String tab) {
        switch (tab) {
            case "users":
            case "posts":
            case "comments":
            case "logins":
            case "messages":
                return tab;
            default:
                return "overview";
        }
    }
}
