--liquibase formatted sql

--changeset Basil:1
CREATE TABLE task (
                    id BIGSERIAL PRIMARY KEY,
                    date TIMESTAMP DEFAULT NOW(),
                    status INT NOT NULL,
                    err_msg VARCHAR ( 50 ) DEFAULT NULL,
                    req_body TEXT DEFAULT NULL);
