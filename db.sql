 CREATE DATABASE task;
 \c task
 CREATE TABLE task (
                   id BIGSERIAL PRIMARY KEY,
                   date TIMESTAMP DEFAULT NOW(),
                   status INT NOT NULL,
                   err_msg VARCHAR ( 50 ) DEFAULT NULL,
                   req_body TEXT DEFAULT NULL);
                   ); 