-- liquibase formatted sql
-- changeset author:Koteron failOnError:true

ALTER TABLE project_statistics DROP COLUMN completed_tasks;
ALTER TABLE project_statistics DROP COLUMN total_tasks;

/* liquibase rollback
ALTER TABLE project_statistics ADD COLUMN completed_tasks int;
ALTER TABLE project_statistics ADD COLUMN total_tasks int;
 */