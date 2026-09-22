DROP TABLE IF EXISTS demo_item;

CREATE TABLE demo_item (
    id BIGINT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    create_by BIGINT NOT NULL,
    create_time TIMESTAMP NOT NULL,
    update_by BIGINT NOT NULL,
    update_time TIMESTAMP NOT NULL,
    deleted INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0
);
