# Papertrail Blog

A Jakarta Servlet/JSP blog with reader, editor, and administrator workflows.

## Setup

1. Create the schema in MySQL (the configured database user must have permission to create the database):

   ```sh
   mysql -u "$DB_USER" -p < db/schema.sql
   ```

2. Configure `DB_URL`, `DB_USER`, and `DB_PASSWORD` for the Tomcat service. For local MySQL using password authentication without TLS, use a URL like:

   ```text
   jdbc:mysql://127.0.0.1:3306/blog_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   ```

   If using systemd, make these variables available to the service with `sudo systemctl edit tomcat11`, then run `sudo systemctl daemon-reload`. Shell exports alone are not inherited by the Tomcat service.

3. Set `ADMIN_EMAIL` to the email address that should receive initial administrator access. Register using that address before registering regular accounts. Without a matching configured address, registrations receive reader access.
4. Build and deploy the WAR to Tomcat 10.1 or newer:

   ```sh
   mvn clean package
   sudo cp target/java-blog.war /var/lib/tomcat11/webapps/
   sudo systemctl restart tomcat11
   ```

For an existing database, back it up and run only migrations that have not already been applied, in date order. These are one-time migrations; do not rerun them:

```sh
mysql -u "$DB_USER" -p blog_db < db/migrations/20261016_add_post_author_id.sql
mysql -u "$DB_USER" -p blog_db < db/migrations/20261017_account_dashboard_schema.sql
mysql -u "$DB_USER" -p blog_db < db/migrations/20261017_admin_contact_message.sql
```

Open `http://localhost:8080/java-blog/`. Register and sign in to like stories, comment, follow editors, and view notifications. Editors can create drafts and publish stories from `/editor`. Administrators can manage accounts, stories, comments, contact messages, and review sign-in history from `/admin`.

## Roles and notifications

- After sign-in, `user` accounts land on `/posts`, editors on `/editor`, and admins on `/admin`. Signed-in accounts can also open `/notifications`.
- Readers can read public stories, like, comment, follow editors, and receive inbox notifications.
- Editors can create and edit their own drafts and publish stories. Their followers are notified when a draft is first published.
- Administrators can manage reader/editor access, hide or restore posts/comments, review logins and contact messages, and receive activity notifications.
- Passwords are stored as PBKDF2 hashes. Forms that change data use session-bound CSRF tokens.

The supplied schema is intended for a fresh database. The app does not automatically alter existing tables; back up and migrate an existing database before applying schema changes.
