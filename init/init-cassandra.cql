CREATE KEYSPACE IF NOT EXISTS message_keyspace WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1}
AND durable_writes = true;

CREATE ROLE IF NOT EXISTS message_user WITH PASSWORD = 'message-user-password' AND LOGIN = true;
GRANT ALL PERMISSIONS ON KEYSPACE message_keyspace TO message_user;
GRANT SELECT, MODIFY ON KEYSPACE message_keyspace TO message_user;

USE message_keyspace;

CREATE TABLE IF NOT EXISTS conversation
(
    user_id                 UUID,
    last_activity_timestamp TIMESTAMP,
    recipient_id            UUID,
    conversation_name       TEXT,
    PRIMARY KEY ((user_id), recipient_id)
) WITH CLUSTERING ORDER BY (recipient_id ASC)
   AND compaction = {'class': 'LeveledCompactionStrategy'}
   AND comment = 'User inbox for 1:1 chats, sorted by most recent activity';

CREATE TABLE IF NOT EXISTS conversation_message
(
    conversation_id   UUID,
    bucket            TEXT,
    message_timestamp TIMESTAMP,
    sender_id         UUID,
    content           TEXT,
    PRIMARY KEY ((conversation_id, bucket), message_timestamp)
) WITH CLUSTERING ORDER BY (message_timestamp DESC)
   AND compaction = {'class': 'TimeWindowCompactionStrategy', 'compaction_window_unit': 'DAYS', 'compaction_window_size': 30}
   AND comment = '1:1 messages partitioned by month (bucket) for efficient time-range reads';

CREATE TABLE IF NOT EXISTS member_by_room
(
    room_id      UUID,
    bucket       INT,
    member_id    UUID,
    display_name TEXT,
    role         TEXT,
    joined_at    TIMESTAMP,
    description  TEXT,
    PRIMARY KEY ((room_id, bucket), member_id)
) WITH compaction = {'class': 'LeveledCompactionStrategy'} AND comment = 'Roster of members in a room';

CREATE TABLE IF NOT EXISTS room_by_member
(
    member_id            UUID,
    room_id              UUID,
    name                 TEXT,
    PRIMARY KEY ((member_id), room_id)
) WITH compaction = {'class': 'LeveledCompactionStrategy'} AND comment = 'Inbox view for group rooms';

CREATE TABLE IF NOT EXISTS room_messages
(
    room_id             UUID,
    time_bucket         TEXT,
    timestamp           TIMESTAMP,
    message_id          UUID,
    sender_id           UUID,
    content             TEXT,
    reply_to_message_id UUID,
    reactions           MAP<TEXT, INT>,
    PRIMARY KEY ((room_id, time_bucket), timestamp, message_id)
) WITH CLUSTERING ORDER BY (timestamp DESC, message_id ASC)
   AND compaction = {'class': 'TimeWindowCompactionStrategy', 'compaction_window_unit': 'DAYS', 'compaction_window_size': 30}
   AND comment = 'Group chat messages, bucketed by time for infinite scaling';

CREATE TABLE IF NOT EXISTS message_reactions
(
    room_id    UUID,
    message_id UUID,
    member_id  UUID,
    emoji      TEXT,
    reacted_at TIMESTAMP,
    PRIMARY KEY ((room_id, message_id), member_id)
) WITH compaction = {'class': 'SizeTieredCompactionStrategy'} AND gc_grace_seconds = 86400
   AND comment = 'Individual user reactions to messages';