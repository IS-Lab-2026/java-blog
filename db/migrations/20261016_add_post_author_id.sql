ALTER TABLE post
ADD COLUMN author_id INT NULL
AFTER blog_id,
    ADD INDEX idx_post_author (author_id),
    ADD CONSTRAINT fk_post_author FOREIGN KEY (author_id) REFERENCES users(user_id) ON DELETE
SET NULL;