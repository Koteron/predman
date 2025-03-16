--liquibase formatted sql

--changeset author:Koteron failOnError:true

ALTER TABLE project_statustics RENAME TO project_statistics;

--rollback ALTER TABLE project_statistics RENAME TO project_statustics;
