CREATE DATABASE IF NOT EXISTS blog_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE blog_db;
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    user_name VARCHAR(200) NOT NULL,
    email VARCHAR(250) NOT NULL UNIQUE,
    password_h VARCHAR(300) NOT NULL,
    user_role VARCHAR(20) NOT NULL DEFAULT 'user',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    join_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB;
CREATE TABLE IF NOT EXISTS categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(300) NOT NULL UNIQUE
) ENGINE = InnoDB;
CREATE TABLE IF NOT EXISTS post (
    blog_id INT AUTO_INCREMENT PRIMARY KEY,
    author_id INT NULL,
    category_id INT NULL,
    title TEXT NOT NULL,
    slug VARCHAR(300) NOT NULL UNIQUE,
    blog_description TEXT NOT NULL,
    body LONGTEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_read VARCHAR(100),
    views INT NOT NULL DEFAULT 0,
    likes INT NOT NULL DEFAULT 0,
    comments INT NOT NULL DEFAULT 0,
    is_visible BOOLEAN NOT NULL DEFAULT TRUE,
    blog_status ENUM('draft', 'live') NOT NULL DEFAULT 'draft',
    CONSTRAINT fk_post_author FOREIGN KEY (author_id) REFERENCES users(user_id) ON DELETE
    SET NULL,
        CONSTRAINT fk_post_category FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE
    SET NULL,
        INDEX idx_post_publication (blog_status, is_visible, created_at),
        INDEX idx_post_author (author_id)
) ENGINE = InnoDB;
CREATE TABLE IF NOT EXISTS subscription (
    user_id INT NOT NULL,
    editor_id INT NOT NULL,
    subscription_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    PRIMARY KEY (user_id, editor_id),
    CONSTRAINT fk_subscription_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_subscription_editor FOREIGN KEY (editor_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE = InnoDB;
CREATE TABLE IF NOT EXISTS likes (
    user_id INT NOT NULL,
    blog_id INT NOT NULL,
    like_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, blog_id),
    CONSTRAINT fk_like_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_like_post FOREIGN KEY (blog_id) REFERENCES post(blog_id) ON DELETE CASCADE
) ENGINE = InnoDB;
CREATE TABLE IF NOT EXISTS notification (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    actor_id INT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    title VARCHAR(250) NOT NULL,
    link VARCHAR(500),
    content TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_notification_actor FOREIGN KEY (actor_id) REFERENCES users(user_id) ON DELETE
    SET NULL,
        INDEX idx_notification_inbox (user_id, is_read, created_at)
) ENGINE = InnoDB;
CREATE TABLE IF NOT EXISTS login (
    login_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    login_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    CONSTRAINT fk_login_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_login_time (login_time)
) ENGINE = InnoDB;
CREATE TABLE IF NOT EXISTS comment (
    comment_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    blog_id INT NOT NULL,
    comment TEXT NOT NULL,
    is_visible BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_post FOREIGN KEY (blog_id) REFERENCES post(blog_id) ON DELETE CASCADE,
    INDEX idx_comment_post (blog_id, is_visible, created_at)
) ENGINE = InnoDB;
CREATE TABLE IF NOT EXISTS contact_message (
    message_id INT AUTO_INCREMENT PRIMARY KEY,
    sender_name VARCHAR(200) NOT NULL,
    sender_email VARCHAR(250) NOT NULL,
    subject VARCHAR(250) NOT NULL,
    message TEXT NOT NULL,
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_resolved BOOLEAN NOT NULL DEFAULT FALSE,
    INDEX idx_contact_inbox (is_resolved, submitted_at)
) ENGINE = InnoDB;