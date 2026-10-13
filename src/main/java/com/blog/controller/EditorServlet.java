package com.blog.controller;

import com.blog.model.Post;
import com.blog.util.AppSecurity;
import com.blog.util.DBConnection;
import com.blog.util.NotificationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

@WebServlet("/editor")
public class EditorServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!AppSecurity.requireRole(request, response, "editor", "admin")) {
            return;
        }
        int userId = AppSecurity.userId(request);
        try (Connection connection = DBConnection.getConnection()) {
            request.setAttribute("posts", listPosts(connection, userId));
            request.setAttribute("categories", loadCategories(connection));
            String id = request.getParameter("id");
            if (id != null) {
                Post post = findOwnedPost(connection, parseId(id), userId);
                if (post == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                request.setAttribute("editPost", post);
            }
            request.setAttribute("csrf", AppSecurity.csrfToken(request.getSession(true)));
            request.getRequestDispatcher("/WEB-INF/views/editor.jsp").forward(request, response);
        } catch (SQLException | ClassNotFoundException exception) {
            getServletContext().log("Could not load editor portal", exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "The editor workspace is unavailable.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        if (!AppSecurity.requireRole(request, response, "editor", "admin")) {
            return;
        }
        if (!AppSecurity.validCsrf(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        int userId = AppSecurity.userId(request);
        String title = clean(request.getParameter("title"));
        String description = clean(request.getParameter("description"));
        String body = request.getParameter("body");
        body = body == null ? "" : body.trim();
        if (title.isEmpty() || title.length() > 500 || description.isEmpty() || description.length() > 2000
                || body.isEmpty() || body.length() > 100_000) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "A title, short description, and story body are required within the allowed lengths.");
            return;
        }
        String status = "publish".equals(request.getParameter("action")) ? "live" : "draft";
        Integer categoryId = nullableInt(request.getParameter("categoryId"));
        String slug = slugify(title);
        try (Connection connection = DBConnection.getConnection()) {
            int postId = parseId(request.getParameter("blogId"));
            boolean wasLive = false;
            if (postId > 0) {
                Post existing = findOwnedPost(connection, postId, userId);
                if (existing == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                wasLive = "live".equals(existing.getBlogStatus());
                slug = uniqueSlug(connection, slug, postId);
                updatePost(connection, postId, userId, categoryId, title, slug, description, body, status);
            } else {
                slug = uniqueSlug(connection, slug, 0);
                postId = insertPost(connection, userId, categoryId, title, slug, description, body, status);
            }
            if ("live".equals(status) && !wasLive) {
                String link = request.getContextPath() + "/post?slug="
                        + URLEncoder.encode(slug, StandardCharsets.UTF_8);
                String author = String.valueOf(request.getSession().getAttribute("userName"));
                NotificationService.notifySubscribers(connection, userId, userId, "New story from " + author,
                        title, link);
                NotificationService.notifyAdmins(connection, userId, "Story published",
                        author + " published “" + title + "”.", request.getContextPath() + "/admin/posts");
            }
        } catch (SQLException | ClassNotFoundException exception) {
            getServletContext().log("Could not save editor story", exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "The story could not be saved.");
            return;
        }
        response.sendRedirect(request.getContextPath() + "/editor?saved=1");
    }

    private List<Post> listPosts(Connection connection, int authorId) throws SQLException {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT blog_id, author_id, category_id, title, slug, blog_description, body, created_at, views, likes, "
                + "comments, blog_status FROM post WHERE author_id = ? ORDER BY updated_at DESC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, authorId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    posts.add(readPost(result));
                }
            }
        }
        return posts;
    }

    private List<Map<String, Object>> loadCategories(Connection connection) throws SQLException {
        List<Map<String, Object>> categories = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT category_id, name FROM categories ORDER BY name");
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                Map<String, Object> category = new LinkedHashMap<>();
                category.put("categoryId", result.getInt("category_id"));
                category.put("name", result.getString("name"));
                categories.add(category);
            }
        }
        return categories;
    }

    private Post findOwnedPost(Connection connection, int postId, int authorId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT blog_id, author_id, category_id, title, slug, blog_description, body, created_at, views, likes, comments, "
                        + "blog_status FROM post WHERE blog_id = ? AND author_id = ?")) {
            statement.setInt(1, postId);
            statement.setInt(2, authorId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? readPost(result) : null;
            }
        }
    }

    private int insertPost(Connection connection, int authorId, Integer categoryId, String title, String slug,
            String description, String body, String status) throws SQLException {
        String sql = "INSERT INTO post (author_id, category_id, title, slug, blog_description, body, blog_status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, authorId);
            setNullableInt(statement, 2, categoryId);
            statement.setString(3, title);
            statement.setString(4, slug);
            statement.setString(5, description);
            statement.setString(6, body);
            statement.setString(7, status);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Story ID was not returned");
                }
                return keys.getInt(1);
            }
        }
    }

    private void updatePost(Connection connection, int postId, int authorId, Integer categoryId, String title,
            String slug, String description, String body, String status) throws SQLException {
        String sql = "UPDATE post SET category_id = ?, title = ?, slug = ?, blog_description = ?, body = ?, "
                + "blog_status = ? WHERE blog_id = ? AND author_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            setNullableInt(statement, 1, categoryId);
            statement.setString(2, title);
            statement.setString(3, slug);
            statement.setString(4, description);
            statement.setString(5, body);
            statement.setString(6, status);
            statement.setInt(7, postId);
            statement.setInt(8, authorId);
            statement.executeUpdate();
        }
    }

    private String uniqueSlug(Connection connection, String base, int currentPostId) throws SQLException {
        String candidate = base;
        int suffix = 2;
        String sql = "SELECT 1 FROM post WHERE slug = ? AND blog_id <> ?";
        while (true) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, candidate);
                statement.setInt(2, currentPostId);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        return candidate;
                    }
                }
            }
            String ending = "-" + suffix++;
            candidate = base.substring(0, Math.min(base.length(), 300 - ending.length())) + ending;
        }
    }

    private Post readPost(ResultSet result) throws SQLException {
        Post post = new Post();
        post.setBlogId(result.getInt("blog_id"));
        post.setAuthorId(result.getInt("author_id"));
        int categoryId = result.getInt("category_id");
        post.setCategoryId(result.wasNull() ? null : categoryId);
        post.setTitle(result.getString("title"));
        post.setSlug(result.getString("slug"));
        post.setBlogDescription(result.getString("blog_description"));
        post.setBody(result.getString("body"));
        post.setCreatedAt(result.getTimestamp("created_at"));
        post.setViews(result.getInt("views"));
        post.setLikes(result.getInt("likes"));
        post.setComments(result.getInt("comments"));
        post.setBlogStatus(result.getString("blog_status"));
        return post;
    }

    private Integer nullableInt(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private void setNullableInt(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value == null) {
            statement.setNull(index, java.sql.Types.INTEGER);
        } else {
            statement.setInt(index, value);
        }
    }

    private int parseId(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private String slugify(String title) {
        String slug = title.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
        if (slug.isEmpty()) {
            slug = "story";
        }
        return slug.substring(0, Math.min(slug.length(), 300));
    }
}
