package com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity

import org.springframework.data.cassandra.core.cql.Ordering
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("messages_by_conversation")
data class MessageByConversation(
    @PrimaryKey val key: MessageByConversationKey,

    @Column("sender_id") val senderId: UUID,

    @Column("content") val content: String
)

@PrimaryKeyClass
data class MessageByConversationKey(
    @PrimaryKeyColumn(
        type = PrimaryKeyType.PARTITIONED, ordinal = 0
    ) val conversationId: UUID,

    @PrimaryKeyColumn(
        type = PrimaryKeyType.PARTITIONED, ordinal = 1
    ) val bucket: Int, // e.g., 202409 for year-month

    @PrimaryKeyColumn(
        type = PrimaryKeyType.CLUSTERED, ordinal = 2, ordering = Ordering.DESCENDING
    ) val messageTimestamp: Instant,
)