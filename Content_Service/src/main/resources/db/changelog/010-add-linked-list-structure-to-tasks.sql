--liquibase formatted sql

--changeset author:Koteron failOnError:true
ALTER TABLE tasks
    ADD COLUMN next uuid;

/* liquibase rollback
ALTER TABLE tasks
    DROP COLUMN next uuid;
*/