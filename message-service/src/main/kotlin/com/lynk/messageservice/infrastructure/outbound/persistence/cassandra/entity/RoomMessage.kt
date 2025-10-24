package com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity

import com.lynk.messageservice.infrastructure.util.BucketUtils.toTimeBucket
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import java.time.Instant
import java.util.UUID

/**
 * Represents a message sent within a room. This uses time based bucketing.
 */
@Table("room_messages")
data class RoomMessage(
    @PrimaryKey val key: RoomMessageKey,
    val sender_id: UUID,
    val content: String,
    val reply_to_message_id: UUID? = null,
    val reactions: Map<String, Int> = emptyMap(),
)

@PrimaryKeyClass
data class RoomMessageKey(
    @PrimaryKeyColumn(name = "room_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    val roomId: UUID,
    @PrimaryKeyColumn(name = "timestamp", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    val timestamp: Instant,
    @PrimaryKeyColumn(name = "time_bucket", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    val timeBucket: String = timestamp.toTimeBucket(),
    @PrimaryKeyColumn(name = "message_id", ordinal = 3, type = PrimaryKeyType.CLUSTERED)
    val messageId: UUID = UUID.randomUUID()
)