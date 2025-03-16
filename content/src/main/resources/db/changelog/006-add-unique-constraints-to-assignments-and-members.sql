-- liquibase formatted sql
-- changeset author:Koteron failOnError:true
ALTER TABLE project_members
    ADD CONSTRAINT unique_user_project
        UNIQUE (user_id, project_id);

ALTER TABLE task_assignments
    ADD CONSTRAINT unique_user_task
        UNIQUE (user_id, task_id);

/* liquibase rollback
ALTER TABLE project_members
    DROP CONSTRAINT unique_user_project;

ALTER TABLE task_assignments
    DROP CONSTRAINT unique_user_task;
 */