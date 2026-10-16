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