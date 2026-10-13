package com.blog.controller;

import com.blog.util.AppSecurity;
import com.blog.util.DBConnection;
import com.blog.util.NotificationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

@WebServlet(urlPatterns = { "/login", "/register", "/logout" })
public class AuthServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        if ("/logout".equals(path)) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        if (AppSecurity.userId(request) > 0) {
            redirectForRole(request, response, AppSecurity.role(request));
            return;
        }
        request.setAttribute("register", "/register".equals(path));
        AppSecurity.csrfToken(request.getSession(true));
        request.getRequestDispatcher("/WEB-INF/views/auth.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!AppSecurity.validCsrf(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        try {
            if ("/logout".equals(request.getServletPath())) {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                response.sendRedirect(request.getContextPath() + "/posts");
            } else if ("/register".equals(request.getServletPath())) {
                register(request, response);
            } else if ("/login".equals(request.getServletPath())) {
                login(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        } catch (SQLException | ClassNotFoundException exception) {
            getServletContext().log("Account request failed", exception);
            request.setAttribute("error", "The account service is temporarily unavailable.");
            request.setAttribute("register", "/register".equals(request.getServletPath()));
            request.getRequestDispatcher("/WEB-INF/views/auth.jsp").forward(request, response);
        }
    }

    private void register(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ClassNotFoundException, ServletException, IOException {
        String name = trim(request.getParameter("name"));
        String email = trim(request.getParameter("email")).toLowerCase(Locale.ROOT);
        String password = value(request.getParameter("password"));
        if (name.isEmpty() || name.length() > 200 || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                || email.length() > 250 || password.length() < 8 || password.length() > 128) {
            showError(request, response, true,
                    "Enter a valid name and email, and a password of at least 8 characters.");
            return;
        }

        try (Connection connection = DBConnection.getConnection()) {
            boolean firstAdmin = acquireBootstrapRole(connection, email);
            String role = firstAdmin ? "admin" : "user";
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO users (user_name, email, password_h, user_role) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, name);
                statement.setString(2, email);
                statement.setString(3, AppSecurity.hashPassword(password));
                statement.setString(4, role);
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("New account ID was not returned");
                    }
                    int userId = keys.getInt(1);
                    if (!firstAdmin) {
                        NotificationService.notifyAdmins(connection, userId, "New account",
                                name + " created an account.", request.getContextPath() + "/admin/users");
                    }
                    signIn(request, userId, name, role);
                }
            } catch (SQLException exception) {
                if ("23000".equals(exception.getSQLState())) {
                    showError(request, response, true, "An account with that email already exists.");
                    return;
                }
                throw exception;
            } finally {
                releaseBootstrapLock(connection);
            }
        }
        redirectForRole(request, response, roleForSession(request));
    }

    private boolean acquireBootstrapRole(Connection connection, String email) throws SQLException {
        String adminEmail = System.getenv("ADMIN_EMAIL");
        if (adminEmail == null || adminEmail.isBlank()) {
            adminEmail = System.getProperty("ADMIN_EMAIL", "");
        }
        if (!email.equalsIgnoreCase(adminEmail.trim())) {
            return false;
        }
        boolean acquired;
        try (PreparedStatement lock = connection.prepareStatement("SELECT GET_LOCK('java_blog_admin_bootstrap', 10)")) {
            try (ResultSet result = lock.executeQuery()) {
                acquired = result.next() && result.getInt(1) == 1;
            }
        }
        if (!acquired) {
            return false;
        }
        try (PreparedStatement admins = connection.prepareStatement(
                "SELECT COUNT(*) FROM users WHERE user_role = 'admin'");
                ResultSet result = admins.executeQuery()) {
            return result.next() && result.getInt(1) == 0;
        }
    }

    private void releaseBootstrapLock(Connection connection) {
        try (PreparedStatement release = connection
                .prepareStatement("SELECT RELEASE_LOCK('java_blog_admin_bootstrap')")) {
            release.execute();
        } catch (SQLException exception) {
            getServletContext().log("Could not release admin bootstrap lock", exception);
        }
    }

    private void login(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ClassNotFoundException, ServletException, IOException {
        String email = trim(request.getParameter("email")).toLowerCase(Locale.ROOT);
        String password = value(request.getParameter("password"));
        String sql = "SELECT user_id, user_name, password_h, user_role, is_active FROM users WHERE email = ?";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next() || !result.getBoolean("is_active")
                        || !AppSecurity.verifyPassword(password, result.getString("password_h"))) {
                    showError(request, response, false, "Email or password is incorrect.");
                    return;
                }
                int userId = result.getInt("user_id");
                String name = result.getString("user_name");
                String role = result.getString("user_role");
                try (PreparedStatement log = connection.prepareStatement(
                        "INSERT INTO login (user_id, ip_address) VALUES (?, ?)")) {
                    log.setInt(1, userId);
                    log.setString(2, request.getRemoteAddr());
                    log.executeUpdate();
                }
                NotificationService.notifyAdmins(connection, userId, "New login",
                        name + " signed in.", request.getContextPath() + "/admin/logins");
                signIn(request, userId, name, role);
            }
        }
        redirectForRole(request, response, roleForSession(request));
    }

    private void signIn(HttpServletRequest request, int userId, String name, String role) {
        HttpSession session = request.getSession(true);
        try {
            request.changeSessionId();
        } catch (IllegalStateException ignored) {
            // A new session already has a fresh identifier.
        }
        session.setAttribute("userId", userId);
        session.setAttribute("userName", name);
        session.setAttribute("role", role);
        session.removeAttribute("csrfToken");
        AppSecurity.csrfToken(session);
    }

    private String roleForSession(HttpServletRequest request) {
        return AppSecurity.role(request);
    }

    private void showError(HttpServletRequest request, HttpServletResponse response, boolean register, String error)
            throws ServletException, IOException {
        request.setAttribute("register", register);
        request.setAttribute("error", error);
        AppSecurity.csrfToken(request.getSession(true));
        request.getRequestDispatcher("/WEB-INF/views/auth.jsp").forward(request, response);
    }

    private void redirectForRole(HttpServletRequest request, HttpServletResponse response, String role)
            throws IOException {
        String destination = "admin".equals(role) ? "/admin" : "editor".equals(role) ? "/editor" : "/posts";
        response.sendRedirect(request.getContextPath() + destination);
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String value(String value) {
        return value == null ? "" : value;
    }
}
