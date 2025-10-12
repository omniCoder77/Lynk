package com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity

import org.springframework.data.cassandra.core.cql.Ordering
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("conversations_by_user")
data class ConversationByUser(
    @PrimaryKey
    val key: ConversationByUserKey,

    @Column("conversation_name")
    val conversationName: String,

    @Column("last_message_preview")
    val lastMessagePreview: String? = null
)

@PrimaryKeyClass
data class ConversationByUserKey(
    @PrimaryKeyColumn(
        "user_id",
        type = PrimaryKeyType.PARTITIONED,
        ordinal = 0
    )
    val userId: UUID,

    @PrimaryKeyColumn(
        "last_activity_timestamp",
        type = PrimaryKeyType.CLUSTERED,
        ordinal = 1,
        ordering = Ordering.DESCENDING
    )
    val lastActivityTimestamp: Instant,
)