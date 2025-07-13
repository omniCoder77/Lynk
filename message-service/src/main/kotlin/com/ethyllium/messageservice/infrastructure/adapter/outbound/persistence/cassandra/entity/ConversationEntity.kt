package com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.entity

import com.ethyllium.messageservice.domain.model.Conversation
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("conversations")
data class ConversationEntity(
    @PrimaryKey("conversation_id") val conversationId: String = UUID.randomUUID().toString(),
    @Column("type") val type: String,
    @Column("last_message_id") val lastMessageId: String,
    @Column("last_message_read_id") val lastMessageReadId: String? = null,
    @Column("last_message_sent_id") val lastMessageSentId: String? = null,
    @Column("created_at") val createdAt: Instant = Instant.now(),
    @Column("updated_at") val updatedAt: Instant = Instant.now(),
)

fun Conversation.toConversationEntity(): ConversationEntity {
    return ConversationEntity(
        conversationId = this.conversationId.value,
        type = this.type.name,
        lastMessageId = this.lastMessageId.value,
        lastMessageReadId = this.lastMessageReadId?.value,
        lastMessageSentId = this.lastMessageSentId?.value,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}