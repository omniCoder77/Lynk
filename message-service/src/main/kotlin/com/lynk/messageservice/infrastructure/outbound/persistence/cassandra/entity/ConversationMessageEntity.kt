package com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity

import com.lynk.messageservice.domain.model.Message
import org.springframework.data.cassandra.core.cql.Ordering
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("conversation_message")
data class ConversationMessageEntity(
    @PrimaryKey val key: MessageKey, val sender_id: UUID, val content: String
) {
    fun toDomain() = Message(
        conversationId = key.conversationId,
        messageTimestamp = key.messageTimestamp,
        senderId = sender_id,
        content = content
    )
}

/**
 * Primary key for the messages table.
 *
 * Partition key: (conversationId, bucket) - ensures messages for a convo in a specific month reside together.
 * Clustering column: messageTimestamp - orders messages within the partition.
 */
@PrimaryKeyClass
data class MessageKey(

    /**
     * Derived using UUIDUtils.getConversationId(user1, user2)
     * Must be PARTITIONED to distribute conversations across nodes.
     */
    @PrimaryKeyColumn(
        name = "conversation_id", type = PrimaryKeyType.PARTITIONED, ordinal = 0
    ) val conversationId: UUID,

    /**
     * Time bucket (e.g., yyyyMM) to prevent partitions from growing too large.
     * Part of Partition Key.
     */
    @PrimaryKeyColumn(
        name = "bucket", type = PrimaryKeyType.PARTITIONED, ordinal = 1
    ) val bucket: String,

    @PrimaryKeyColumn(
        name = "message_timestamp", type = PrimaryKeyType.CLUSTERED, ordinal = 2, ordering = Ordering.DESCENDING
    ) val messageTimestamp: Instant,
)