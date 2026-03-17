-- Strengthen relationship between audit_logs and users by adding a foreign key
-- from audit_logs.actor_user_id to users.id. We use ON DELETE SET NULL so that
-- historical audit data is preserved even if a user is deleted.

ALTER TABLE audit_logs
    ADD CONSTRAINT fk_audit_logs_actor_user
        FOREIGN KEY (actor_user_id)
            REFERENCES users(id)
            ON DELETE SET NULL;

