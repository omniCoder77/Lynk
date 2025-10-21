package com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity

import com.lynk.messageservice.domain.model.Conversation
import org.springframework.data.cassandra.core.cql.Ordering
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("conversation")
data class ConversationEntity(
    @PrimaryKey val key: ConversationKey,
    val conversationName: String,
    val lastMessagePreview: String? = null
) {
    fun toDomain() = Conversation(
        userId = key.userId,
        recipientId = key.recipientId,
        lastActivityTimestamp = key.lastActivityTimestamp,
        conversationName = conversationName,
        lastMessagePreview = lastMessagePreview
    )
}

@PrimaryKeyClass
data class ConversationKey(
    @PrimaryKeyColumn(
        name = "user_id", type = PrimaryKeyType.PARTITIONED, ordinal = 0
    ) val userId: UUID,
    @PrimaryKeyColumn(
        name = "recipient_id", type = PrimaryKeyType.CLUSTERED, ordinal = 2
    )
    val recipientId: UUID,
    @PrimaryKeyColumn(
        name = "last_activity_timestamp", type = PrimaryKeyType.CLUSTERED, ordinal = 1, ordering = Ordering.DESCENDING
    ) val lastActivityTimestamp: Instant
)