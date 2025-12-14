CREATE DATABASE auth_db;

\c auth_db;
CREATE USER auth_service;
GRANT CONNECT ON DATABASE auth_db TO auth_service;
GRANT ALL PRIVILEGES ON DATABASE auth_db TO auth_service;
GRANT ALL PRIVILEGES ON TABLE users TO auth_service;
CREATE USER room_service;
GRANT CONNECT ON DATABASE room_db TO room_service;
GRANT ALL PRIVILEGES ON DATABASE room_db TO room_service;
GRANT ALL PRIVILEGES ON TABLE room TO room_service;
GRANT ALL PRIVILEGES ON TABLE membership TO room_service;
GRANT ALL PRIVILEGES ON TABLE banned_users TO room_service;

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

CREATE DATABASE user_db;
\c user_db;

CREATE USER user_service WITH PASSWORD 'user_service_password';
GRANT CONNECT ON DATABASE user_db TO user_service;
GRANT ALL PRIVILEGES ON DATABASE user_db TO user_service;
CREATE TABLE IF NOT EXISTS users (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50) NOT NULL,
    bio varchar(500),
    profile VARCHAR(500),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_phone_number UNIQUE (phone_number)
);

CREATE INDEX IF NOT EXISTS idx_users_user_id ON users(user_id);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS room (
    room_id UUID PRIMARY KEY,
    room_name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS member (
    member_id UUID PRIMARY KEY,
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    role VARCHAR(50) NOT NULL,
    is_allowed_to_message BOOLEAN NOT NULL DEFAULT TRUE,
    is_allowed_to_send_media BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_member_user FOREIGN KEY (member_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT chk_member_role CHECK (role IN ('ADMIN', 'MODERATOR', 'MEMBER'))
);

CREATE TABLE IF NOT EXISTS conversation (
    conversation_id UUID PRIMARY KEY,
    sender_id UUID NOT NULL,
    recipient_id UUID NOT NULL,
    is_blocked BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_conversation_sender FOREIGN KEY (sender_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_conversation_recipient FOREIGN KEY (recipient_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT chk_conversation_participants CHECK (sender_id <> recipient_id)
);

CREATE INDEX IF NOT EXISTS idx_conversation_sender ON conversation(sender_id);

CREATE INDEX IF NOT EXISTS idx_conversation_recipient ON conversation(recipient_id);
CREATE TABLE IF NOT EXISTS blocklist (
    blocklist_id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    blocked_user_id UUID NOT NULL,
    CONSTRAINT fk_blocklist_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_blocklist_blocked_user FOREIGN KEY (blocked_user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT chk_blocklist_self CHECK (user_id <> blocked_user_id),
    CONSTRAINT uq_blocklist_entry UNIQUE (user_id, blocked_user_id)
);

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE room (
                      room_id UUID PRIMARY KEY,
                      name VARCHAR(255) NOT NULL,
                      max_size INTEGER NOT NULL DEFAULT 100,
                      visibility VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
                      CONSTRAINT uq_room_name UNIQUE (name),
                      CONSTRAINT chk_room_max_size CHECK (max_size > 0, max_size <= 1000)
);
CREATE INDEX idx_room_name ON room(name);

CREATE TABLE membership (
                            membership_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            user_id UUID NOT NULL,
                            room_id UUID NOT NULL,
                            joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                            role varchar(10) NOT NULL,
                            CONSTRAINT fk_membership_room
                                FOREIGN KEY (room_id)
                                    REFERENCES room(room_id)
                                    ON DELETE CASCADE,
                            CONSTRAINT uq_membership_user_room UNIQUE (user_id, room_id)
);
CREATE INDEX idx_membership_room_id ON membership(room_id);
CREATE INDEX idx_membership_user_id ON membership(user_id);
CREATE TABLE banned_users (
                              banned_id UUID PRIMARY KEY,
                              user_id UUID NOT NULL,
                              room_id UUID NOT NULL,
                              reason TEXT NOT NULL DEFAULT '',
                              banned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                              banned_until TIMESTAMPTZ,
                              CONSTRAINT fk_ban_room
                                  FOREIGN KEY (room_id)
                                      REFERENCES room (room_id)
                                      ON DELETE CASCADE,
                              CONSTRAINT uq_banned_user_room UNIQUE (user_id, room_id)
);
CREATE INDEX idx_banned_users_lookup ON banned_users(user_id, room_id);

CREATE INDEX IF NOT EXISTS idx_blocklist_user_id ON blocklist(user_id);

GRANT ALL PRIVILEGES ON TABLE users TO user_service;
GRANT ALL PRIVILEGES ON TABLE member TO user_service;
GRANT ALL PRIVILEGES ON TABLE conversation TO user_service;
GRANT ALL PRIVILEGES ON TABLE blocklist TO user_service;