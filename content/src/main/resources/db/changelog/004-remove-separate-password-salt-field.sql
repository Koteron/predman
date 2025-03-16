-- liquibase formatted sql
-- changeset author:Koteron failOnError:true
ALTER TABLE users DROP COLUMN password_salt;

-- rollback ALTER TABLE users ADD COLUMN password_salt VARCHAR;
