package com.blog.controller;

import com.blog.util.AppSecurity;
import com.blog.util.DBConnection;
import com.blog.util.NotificationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Locale;

@WebServlet("/contact")
public class ContactServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        AppSecurity.csrfToken(request.getSession(true));
        request.getRequestDispatcher("/WEB-INF/views/contact.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        if (!AppSecurity.validCsrf(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String name = trim(request.getParameter("name"));
        String email = trim(request.getParameter("email")).toLowerCase(Locale.ROOT);
        String subject = trim(request.getParameter("subject"));
        String message = trim(request.getParameter("message"));
        if (name.isEmpty() || name.length() > 200 || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                || email.length() > 250 || subject.isEmpty() || subject.length() > 250
                || message.isEmpty() || message.length() > 5000) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Please complete every field with valid details.");
            return;
        }
        String sql = "INSERT INTO contact_message (sender_name, sender_email, subject, message) VALUES (?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, subject);
            statement.setString(4, message);
            statement.executeUpdate();
            NotificationService.notifyAdmins(connection, AppSecurity.userId(request) == 0
                    ? null
                    : AppSecurity.userId(request), "New contact message", subject,
                    request.getContextPath() + "/admin?tab=messages");
            response.sendRedirect(request.getContextPath() + "/contact?sent=1");
        } catch (Exception exception) {
            getServletContext().log("Could not save contact message", exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Your message could not be sent. Please try again later.");
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
