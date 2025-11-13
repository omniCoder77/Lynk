CREATE DATABASE auth_db;

CREATE USER auth_user WITH PASSWORD 'auth_user_password';
GRANT CONNECT ON DATABASE auth_db TO auth_user;
GRANT ALL PRIVILEGES ON DATABASE auth_db TO auth_user;

CREATE TABLE users
(
    user_id            UUID PRIMARY KEY default gen_random_uuid(),
    totp_secret        VARCHAR(255) default null,
    phone_number       VARCHAR(32)              NOT NULL UNIQUE,
    first_name         VARCHAR(64)              NOT NULL,
    last_name          VARCHAR(64)              NOT NULL,
    created_date       TIMESTAMP WITH TIME ZONE NOT NULL,
    last_modified_date TIMESTAMP WITH TIME ZONE NOT NULL,
    enabled            BOOLEAN      DEFAULT FALSE,
    is_account_locked  BOOLEAN      DEFAULT FALSE,
    role               VARCHAR(128) DEFAULT 'USER'
);

create index idx_users_phone_number on users (phone_number);