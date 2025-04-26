--liquibase formatted sql
-- changeset vkrasnov:1

CREATE TABLE IF NOT EXISTS notification_task
(
    id                    BIGSERIAL PRIMARY KEY,
    chat_id               BIGINT       NOT NULL,
    message               TEXT NOT NULL,
    notification_datetime TIMESTAMP    NOT NULL
);