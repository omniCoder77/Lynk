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
    @Column("last_message_id") val last_message_id: String,
    @Column("last_message_read_id") val last_message_read_id: String? = null,
    @Column("last_message_sent_id") val last_message_sent_id: String? = null,
    @Column("created_at") val created_at: Instant = Instant.now(),
    @Column("updated_at") val updated_at: Instant = Instant.now(),
)

fun Conversation.toConversationEntity(): ConversationEntity {
    return ConversationEntity(
        conversationId = this.conversationId.value,
        type = this.type.name,
        last_message_id = this.lastMessageId.value,
        last_message_read_id = this.lastMessageReadId?.value,
        last_message_sent_id = this.lastMessageSentId?.value,
        created_at = this.createdAt,
        updated_at = this.updatedAt
    )
}