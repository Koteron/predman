-- liquibase formatted sql
-- changeset author:Koteron failOnError:true

ALTER TABLE project_statistics DROP CONSTRAINT project_statustics_pkey;
ALTER TABLE project_statistics ADD COLUMN id uuid default gen_random_uuid();
ALTER TABLE project_statistics ADD CONSTRAINT project_statistics_pkey PRIMARY KEY (id);
ALTER TABLE project_statistics RENAME COLUMN updated_at TO saved_at;

/* liquibase rollback
ALTER TABLE project_statistics RENAME COLUMN saved_at TO updated_at;
ALTER TABLE project_statistics DROP CONSTRAINT project_statistics_pkey;
ALTER TABLE project_statistics DROP COLUMN id;
ALTER TABLE project_statistics ADD CONSTRAINT project_statustics_pkey PRIMARY KEY (project_id);
 */