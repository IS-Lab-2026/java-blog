# Papertrail Blog

A Jakarta Servlet/JSP blog with reader, editor, and administrator workflows.

## Setup

1. Create the schema in MySQL:

   ```sh
   mysql -u "$DB_USER" -p < db/schema.sql
   ```

2. Configure database access with `DB_URL`, `DB_USER`, and `DB_PASSWORD`. The defaults target `jdbc:mysql://localhost:3306/blog_db`, user `root`, and an empty password for local development.
3. Set `ADMIN_EMAIL` to the email address that should receive initial administrator access. Register using that address before registering regular accounts. Without a matching configured address, registrations receive reader access.
4. Build and deploy the WAR to a Jakarta EE 10-compatible servlet container such as Tomcat 10.1 or newer:

   ```sh
   mvn clean package
   ```

For an existing database created before post authors were added, back it up and run the one-time migration:

```sh
mysql -u "$DB_USER" -p blog_db < db/migrations/20261016_add_post_author_id.sql
```

The public journal is available at the application context root. Register and sign in to like stories, comment, follow editors, and view notifications. Editors can create drafts and publish stories from `/editor`. Administrators can manage accounts, stories, comments, contact messages, and review sign-in history from `/admin`.

## Roles and notifications

- Readers can read public stories, like, comment, follow editors, and receive inbox notifications.
- Editors can create and edit their own drafts and publish stories. Their followers are notified when a draft is first published.
- Administrators can manage reader/editor access, hide or restore posts/comments, review logins and contact messages, and receive activity notifications.
- Passwords are stored as PBKDF2 hashes. Forms that change data use session-bound CSRF tokens.

The supplied schema is intended for a fresh database. The app does not automatically alter existing tables; back up and migrate an existing database before applying schema changes.
