--liquibase formatted sql

--changeset author:Koteron update_project_statistics_structure failOnError:true
ALTER TABLE project_statistics
    DROP COLUMN easy_tasks,
    DROP COLUMN medium_tasks,
    DROP COLUMN hard_tasks,
    DROP COLUMN completed_easy_tasks,
    DROP COLUMN completed_medium_tasks,
    DROP COLUMN completed_hard_tasks,
    DROP COLUMN estimated_date,
    DROP COLUMN completion_probability,
    DROP COLUMN days_to_deadline;

ALTER TABLE project_statistics
    ADD COLUMN remaining_tasks int,
    ADD COLUMN remaining_story_points double precision,
    ADD COLUMN dependency_coefficient double precision,
    ADD COLUMN critical_path_length double precision,
    ADD COLUMN sum_experience double precision,
    ADD COLUMN available_hours double precision,
    ADD COLUMN external_risk_probability double precision;

ALTER TABLE tasks
    DROP COLUMN complexity;

ALTER TABLE tasks
    ADD COLUMN story_points double precision;

CREATE TABLE IF NOT EXISTS task_dependencies (
     id uuid primary key default gen_random_uuid(),
     task_id uuid NOT NULL,
     dependency_id uuid NOT NULL,
     CONSTRAINT fk_task
         FOREIGN KEY (task_id)
             REFERENCES tasks (id)
             ON DELETE CASCADE,
     CONSTRAINT fk_dependency
         FOREIGN KEY (dependency_id)
             REFERENCES tasks (id)
             ON DELETE CASCADE,
     CONSTRAINT unique_dependency
         UNIQUE (task_id, dependency_id)
);

ALTER TABLE projects
    ADD COLUMN predicted_deadline date,
    ADD COLUMN certainty_percent double precision,
    ADD COLUMN available_hours double precision,
    ADD COLUMN sum_experience double precision,
    ADD COLUMN external_risk_probability double precision;

/* liquibase rollback
ALTER TABLE project_statistics
    ADD COLUMN total_tasks int,
    ADD COLUMN completed_tasks int,
    ADD COLUMN easy_tasks int,
    ADD COLUMN medium_tasks int,
    ADD COLUMN hard_tasks int,
    ADD COLUMN completed_easy_tasks int,
    ADD COLUMN completed_medium_tasks int,
    ADD COLUMN completed_hard_tasks int;

ALTER TABLE project_statistics
    DROP COLUMN remaining_tasks,
    DROP COLUMN remaining_story_points,
    DROP COLUMN dependency_coefficient,
    DROP COLUMN critical_path_length,
    DROP COLUMN sum_experience,
    DROP COLUMN available_hours,
    DROP COLUMN external_risk_probability;

ALTER TABLE tasks
    ADD COLUMN complexity varchar(32) NOT NULL DEFAULT 'MEDIUM' CHECK (complexity IN ('EASY', 'MEDIUM', 'HARD'));

ALTER TABLE tasks
    DROP COLUMN story_points;

DROP TABLE IF EXISTS task_dependencies;
*/