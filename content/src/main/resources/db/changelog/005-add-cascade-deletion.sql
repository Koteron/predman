-- liquibase formatted sql
-- changeset author:Koteron failOnError:true
ALTER TABLE project_members
    DROP CONSTRAINT project_members_fk2,
    ADD CONSTRAINT project_members_fk2
        FOREIGN KEY (project_id) REFERENCES projects(id)
            ON DELETE CASCADE;

ALTER TABLE project_members
    DROP CONSTRAINT project_members_fk1,
    ADD CONSTRAINT project_members_fk1
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON DELETE CASCADE;

ALTER TABLE project_statistics
    DROP CONSTRAINT project_statistics_fk1,
    ADD constraint project_statistics_fk1
        foreign key (project_id) references projects (id)
            ON DELETE CASCADE;

ALTER TABLE tasks
    DROP CONSTRAINT tasks_fk1,
    ADD constraint tasks_fk1
        foreign key (project_id) references projects (id)
            ON DELETE CASCADE;

ALTER TABLE task_assignments
    DROP CONSTRAINT project_members_fk1,
    ADD constraint project_members_fk1
        foreign key (task_id) references tasks (id)
            ON DELETE CASCADE;

ALTER TABLE task_assignments
    DROP CONSTRAINT project_members_fk2,
    ADD constraint project_members_fk2
        foreign key (user_id) references users (id)
            ON DELETE CASCADE;

/* liquibase rollback
ALTER TABLE project_members
    DROP CONSTRAINT project_members_fk2,
    ADD CONSTRAINT project_members_fk2
        FOREIGN KEY (project_id) REFERENCES projects(id);
ALTER TABLE project_members
    DROP CONSTRAINT project_members_fk1,
    ADD CONSTRAINT project_members_fk1
        FOREIGN KEY (user_id) REFERENCES user(id);

   ALTER TABLE project_statistics
    DROP CONSTRAINT project_statistics_fk1,
    ADD constraint project_statistics_fk1
        foreign key (project_id) references projects (id);

ALTER TABLE tasks
    DROP CONSTRAINT tasks_fk1,
    ADD constraint tasks_fk1
        foreign key (project_id) references projects (id);

ALTER TABLE task_assignments
    DROP CONSTRAINT project_members_fk1,
    ADD constraint project_members_fk1
        foreign key (task_id) references tasks (id);

ALTER TABLE task_assignments
    DROP CONSTRAINT project_members_fk2,
    ADD constraint project_members_fk2
        foreign key (user_id) references users (id);
 */