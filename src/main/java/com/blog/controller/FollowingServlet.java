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

@WebServlet("/following")
public class FollowingServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!AppSecurity.requireRole(request, response, "user")) {
            return;
        }
        String sql = "SELECT u.user_id, u.user_name, s.subscription_date, COUNT(p.blog_id) AS story_count "
                + "FROM subscription s JOIN users u ON u.user_id = s.editor_id AND u.user_role = 'editor' "
                + "AND u.is_active = TRUE LEFT JOIN post p ON p.author_id = u.user_id "
                + "AND p.blog_status = 'live' AND p.is_visible = TRUE "
                + "WHERE s.user_id = ? GROUP BY u.user_id, u.user_name, s.subscription_date "
                + "ORDER BY s.subscription_date DESC, u.user_name";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, AppSecurity.userId(request));
            List<Map<String, Object>> editors = new ArrayList<>();
            try (ResultSet result = statement.executeQuery()) {
                int columns = result.getMetaData().getColumnCount();
                while (result.next()) {
                    Map<String, Object> editor = new LinkedHashMap<>();
                    for (int column = 1; column <= columns; column++) {
                        editor.put(result.getMetaData().getColumnLabel(column), result.getObject(column));
                    }
                    editors.add(editor);
                }
            }
            request.setAttribute("editors", editors);
            request.getRequestDispatcher("/WEB-INF/views/following.jsp").forward(request, response);
        } catch (SQLException | ClassNotFoundException exception) {
            getServletContext().log("Could not load followed editors", exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Your followed editors are unavailable.");
        }
    }
}