package com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity

import com.lynk.messageservice.domain.model.Conversation
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.*
import java.time.Instant
import java.util.*

@Table("conversation")
data class ConversationEntity(
    @PrimaryKey val key: ConversationKey,
    val lastActivityTimestamp: Instant
) {
    fun toDomain() = Conversation(
        userId = key.userId,
        recipientId = key.recipientId,
        lastActivityTimestamp = lastActivityTimestamp,
    )
}

@PrimaryKeyClass
data class ConversationKey(
    @PrimaryKeyColumn(
        name = "user_id", type = PrimaryKeyType.PARTITIONED, ordinal = 0
    ) val userId: UUID,
    @PrimaryKeyColumn(
        name = "recipient_id", type = PrimaryKeyType.CLUSTERED, ordinal = 1
    ) val recipientId: UUID,
)