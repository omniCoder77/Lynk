package com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity

import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("message_reactions")
data class MessageReaction(
    @PrimaryKey val key: MessageReactionKey,
    val reacted_at: Instant = Instant.now(),
    val emoji: String,
)

@PrimaryKeyClass
data class MessageReactionKey(
    @PrimaryKeyColumn(name = "room_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED) val roomId: UUID,
    @PrimaryKeyColumn(name = "message_id", ordinal = 1, type = PrimaryKeyType.PARTITIONED) val messageId: UUID,
    @PrimaryKeyColumn(name = "member_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED) val memberId: UUID
)