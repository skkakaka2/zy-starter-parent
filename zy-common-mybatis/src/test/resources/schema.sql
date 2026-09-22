DROP TABLE IF EXISTS test_account;

CREATE TABLE test_account (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    create_by BIGINT NOT NULL,
    create_time TIMESTAMP NOT NULL,
    update_by BIGINT NOT NULL,
    update_time TIMESTAMP NOT NULL,
    deleted INTEGER NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0
);
