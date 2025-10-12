package com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity

import org.springframework.data.cassandra.core.cql.Ordering
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("conversations_by_user")
data class Conversation(
    @PrimaryKey val key: ConversationKey,
    val conversationName: String,
    val lastMessagePreview: String? = null
)

@PrimaryKeyClass
data class ConversationKey(
    @PrimaryKeyColumn(
        name = "user_id", type = PrimaryKeyType.PARTITIONED, ordinal = 0
    ) val userId: UUID,
    @PrimaryKeyColumn(
        name = "recipient_id", type = PrimaryKeyType.CLUSTERED, ordinal = 2
    )
    val recipientId: String,
    @PrimaryKeyColumn(
        name = "last_activity_timestamp", type = PrimaryKeyType.CLUSTERED, ordinal = 1, ordering = Ordering.DESCENDING
    ) val lastActivityTimestamp: Instant
)