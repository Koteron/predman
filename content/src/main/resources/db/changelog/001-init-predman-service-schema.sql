--liquibase formatted sql

--changeset author:Koteron failOnError:true
create extension if not exists "uuid-ossp";

create table if not exists users
(
    id              uuid primary key   default gen_random_uuid(),
    login           varchar,
    email           varchar unique not null,
    created_date    timestamp,
    updated_date    timestamp,
    password_salt   varchar,
    password_hash   varchar    
);

create table if not exists projects
(
    id              uuid primary key   default gen_random_uuid(),
    name            varchar,
    description     varchar,
    created_date    timestamp,
    updated_date    timestamp,
    due_date        date,
    owner_id        uuid,
    
    constraint projects_fk1 foreign key (owner_id) references users (id)
);

create table if not exists project_statustics
(
    project_id             uuid primary key   default gen_random_uuid(),
    total_tasks            int,
    completed_tasks        int,
    easy_tasks             int,
    medium_tasks           int,
    hard_tasks             int,
    completed_easy_tasks   int,
    completed_medium_tasks int,
    completed_hard_tasks   int,
    team_size              int,
    days_since_start       int,
    days_to_deadline       int,
    completion_probability double precision,
    estimated_date         date,
    updated_at             timestamp,
    
    constraint project_statistics_fk1 foreign key (project_id) references projects (id)
);

create table if not exists project_members
(
    id              uuid primary key   default gen_random_uuid(),
    user_id         uuid not null,
    project_id      uuid not null,
    joined_at       timestamp,
    
    constraint project_members_fk1 foreign key (user_id) references users (id),
    constraint project_members_fk2 foreign key (project_id) references projects (id)
);

create table if not exists tasks
(
    id              uuid primary key   default gen_random_uuid(),
    project_id      uuid not null,
    name            varchar,
    description     varchar,
    complexity      varchar(32) not null check ( complexity in ('EASY', 'MEDIUM', 'HARD')) DEFAULT 'MEDIUM',
    status          varchar(32) not null check ( status in ('PLANNED', 'IN_PROGRESS', 'COMPLETED')) DEFAULT 'PLANNED',
    created_at      timestamp,
    updated_at      timestamp,
    
    constraint tasks_fk1 foreign key (project_id) references projects (id)
);

create table if not exists task_assignments
(
    id              uuid primary key   default gen_random_uuid(),
    user_id         uuid not null,
    task_id         uuid not null,
    joined_at       timestamp,
    
    constraint project_members_fk1 foreign key (user_id) references users (id),
    constraint project_members_fk2 foreign key (task_id) references tasks (id)
);

-----

-- rollback drop table tasks;
-- rollback drop table task_assignments;
-- rollback drop table users;
-- rollback drop table projects;
-- rollback drop table project_members;
-- rollback drop table project_statustics;
-- rollback drop extension "uuid-ossp";

