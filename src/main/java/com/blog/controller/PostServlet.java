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
import java.sql.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = { "/posts", "/post" })
public class PostServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        AppSecurity.csrfToken(request.getSession(true));
        if ("/post".equals(request.getServletPath())) {
            showPost(request, response);
        } else {
            showPosts(request, response);
        }
    }

    private void showPosts(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String sql = "SELECT p.blog_id, p.author_id, p.title, p.slug, p.blog_description, p.body, p.created_at, "
                + "p.views, p.likes, p.comments, p.blog_status, u.user_name AS author_name "
                + "FROM post p LEFT JOIN users u ON u.user_id = p.author_id "
                + "WHERE p.blog_status = 'live' AND p.is_visible = TRUE ORDER BY p.created_at DESC";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            request.setAttribute("posts", readPosts(result));
            request.getRequestDispatcher("/index.jsp").forward(request, response);
        } catch (SQLException | ClassNotFoundException exception) {
            getServletContext().log("Could not load the blog", exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "The blog is temporarily unavailable.");
        }
    }

    private void showPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String slug = request.getParameter("slug");
        if (slug == null || slug.length() > 300) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String sql = "SELECT p.blog_id, p.author_id, p.title, p.slug, p.blog_description, p.body, p.created_at, "
                + "p.views, p.likes, p.comments, p.blog_status, u.user_name AS author_name "
                + "FROM post p LEFT JOIN users u ON u.user_id = p.author_id "
                + "WHERE p.slug = ? AND p.blog_status = 'live' AND p.is_visible = TRUE";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, slug);
            Post post;
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                post = readPost(result);
            }
            try (PreparedStatement update = connection.prepareStatement(
                    "UPDATE post SET views = views + 1 WHERE blog_id = ?")) {
                update.setInt(1, post.getBlogId());
                update.executeUpdate();
            }
            post.setViews(post.getViews() + 1);
            request.setAttribute("post", post);
            request.setAttribute("commentsList", loadComments(connection, post.getBlogId()));
            int userId = AppSecurity.userId(request);
            request.setAttribute("liked", userId > 0 && exists(connection,
                    "SELECT 1 FROM likes WHERE user_id = ? AND blog_id = ?", userId, post.getBlogId()));
            request.setAttribute("following", userId > 0 && post.getAuthorId() != null
                    && exists(connection, "SELECT 1 FROM subscription WHERE user_id = ? AND editor_id = ?",
                            userId, post.getAuthorId()));
            request.setAttribute("commentCount", post.getComments());
            request.getRequestDispatcher("/WEB-INF/views/blog-detail.jsp").forward(request, response);
        } catch (SQLException | ClassNotFoundException exception) {
            getServletContext().log("Could not load blog post", exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "This story is temporarily unavailable.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        int userId = AppSecurity.userId(request);
        if (userId == 0) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        if (!AppSecurity.validCsrf(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String action = request.getParameter("action");
        String slug;
        try (Connection connection = DBConnection.getConnection()) {
            int postId = parseId(request.getParameter("blogId"));
            Post post = findPublicPost(connection, postId);
            if (post == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            slug = post.getSlug();
            if ("like".equals(action)) {
                toggleLike(connection, post, userId, request);
            } else if ("comment".equals(action)) {
                addComment(connection, post, userId, request);
            } else if ("subscribe".equals(action)) {
                subscribe(connection, post, userId, request);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        } catch (SQLException | ClassNotFoundException exception) {
            getServletContext().log("Could not save blog interaction", exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Your action could not be saved.");
            return;
        }
        response.sendRedirect(request.getContextPath() + "/post?slug="
                + URLEncoder.encode(slug, StandardCharsets.UTF_8) + "#story-actions");
    }

    private void toggleLike(Connection connection, Post post, int userId, HttpServletRequest request)
            throws SQLException {
        boolean liked = exists(connection, "SELECT 1 FROM likes WHERE user_id = ? AND blog_id = ?",
                userId, post.getBlogId());
        if (liked) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM likes WHERE user_id = ? AND blog_id = ?")) {
                statement.setInt(1, userId);
                statement.setInt(2, post.getBlogId());
                statement.executeUpdate();
            }
        } else {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO likes (user_id, blog_id) VALUES (?, ?)")) {
                statement.setInt(1, userId);
                statement.setInt(2, post.getBlogId());
                statement.executeUpdate();
            }
            if (post.getAuthorId() != null && post.getAuthorId() != userId) {
                NotificationService.notifyUser(connection, post.getAuthorId(), userId, "Your story was liked",
                        request.getSession().getAttribute("userName") + " liked “" + post.getTitle() + "”.",
                        request.getContextPath() + "/post?slug=" + post.getSlug());
            }
            NotificationService.notifyAdmins(connection, userId, "Post liked",
                    request.getSession().getAttribute("userName") + " liked “" + post.getTitle() + "”.",
                    request.getContextPath() + "/admin/posts");
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE post SET likes = (SELECT COUNT(*) FROM likes WHERE blog_id = ?) WHERE blog_id = ?")) {
            statement.setInt(1, post.getBlogId());
            statement.setInt(2, post.getBlogId());
            statement.executeUpdate();
        }
    }

    private void addComment(Connection connection, Post post, int userId, HttpServletRequest request)
            throws SQLException, IOException {
        String text = request.getParameter("comment");
        if (text == null || text.trim().isEmpty() || text.length() > 3000) {
            return;
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO comment (user_id, blog_id, comment) VALUES (?, ?, ?)")) {
            statement.setInt(1, userId);
            statement.setInt(2, post.getBlogId());
            statement.setString(3, text.trim());
            statement.executeUpdate();
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE post SET comments = (SELECT COUNT(*) FROM comment WHERE blog_id = ?) WHERE blog_id = ?")) {
            statement.setInt(1, post.getBlogId());
            statement.setInt(2, post.getBlogId());
            statement.executeUpdate();
        }
        String actor = String.valueOf(request.getSession().getAttribute("userName"));
        String link = request.getContextPath() + "/post?slug=" + post.getSlug() + "#comments";
        if (post.getAuthorId() != null && post.getAuthorId() != userId) {
            NotificationService.notifyUser(connection, post.getAuthorId(), userId, "New comment",
                    actor + " commented on “" + post.getTitle() + "”.", link);
        }
        NotificationService.notifyAdmins(connection, userId, "New blog comment",
                actor + " commented on “" + post.getTitle() + "”.", request.getContextPath() + "/admin/comments");
    }

    private void subscribe(Connection connection, Post post, int userId, HttpServletRequest request)
            throws SQLException {
        if (post.getAuthorId() == null || post.getAuthorId() == userId) {
            return;
        }
        try (PreparedStatement editor = connection.prepareStatement(
                "SELECT 1 FROM users WHERE user_id = ? AND user_role = 'editor' AND is_active = TRUE")) {
            editor.setInt(1, post.getAuthorId());
            try (ResultSet result = editor.executeQuery()) {
                if (!result.next()) {
                    return;
                }
            }
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT IGNORE INTO subscription (user_id, editor_id) VALUES (?, ?)")) {
            statement.setInt(1, userId);
            statement.setInt(2, post.getAuthorId());
            if (statement.executeUpdate() > 0) {
                NotificationService.notifyUser(connection, post.getAuthorId(), userId, "New subscriber",
                        request.getSession().getAttribute("userName") + " subscribed to your stories.",
                        request.getContextPath() + "/editor");
            }
        }
    }

    private Post findPublicPost(Connection connection, int blogId) throws SQLException {
        String sql = "SELECT p.blog_id, p.author_id, p.title, p.slug, p.blog_description, p.body, p.created_at, "
                + "p.views, p.likes, p.comments, p.blog_status, u.user_name AS author_name "
                + "FROM post p LEFT JOIN users u ON u.user_id = p.author_id "
                + "WHERE p.blog_id = ? AND p.blog_status = 'live' AND p.is_visible = TRUE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, blogId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? readPost(result) : null;
            }
        }
    }

    private List<Post> loadComments(Connection connection, int blogId) throws SQLException {
        List<Post> comments = new ArrayList<>();
        String sql = "SELECT c.comment_id, c.comment, c.created_at, u.user_name FROM comment c "
                + "JOIN users u ON u.user_id = c.user_id WHERE c.blog_id = ? AND c.is_visible = TRUE "
                + "ORDER BY c.created_at";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, blogId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Post comment = new Post();
                    comment.setBlogId(result.getInt("comment_id"));
                    comment.setBody(result.getString("comment"));
                    comment.setAuthorName(result.getString("user_name"));
                    comment.setCreatedAt(result.getTimestamp("created_at"));
                    comments.add(comment);
                }
            }
        }
        return comments;
    }

    private boolean exists(Connection connection, String sql, int first, int second) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, first);
            statement.setInt(2, second);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private List<Post> readPosts(ResultSet result) throws SQLException {
        List<Post> posts = new ArrayList<>();
        while (result.next()) {
            posts.add(readPost(result));
        }
        return posts;
    }

    private Post readPost(ResultSet result) throws SQLException {
        Post post = new Post();
        post.setBlogId(result.getInt("blog_id"));
        int authorId = result.getInt("author_id");
        post.setAuthorId(result.wasNull() ? null : authorId);
        post.setAuthorName(result.getString("author_name"));
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

    private int parseId(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}