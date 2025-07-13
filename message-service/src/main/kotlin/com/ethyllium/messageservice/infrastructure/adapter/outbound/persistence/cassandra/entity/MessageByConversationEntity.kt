package com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.entity

import com.ethyllium.messageservice.domain.model.Message
import org.springframework.data.cassandra.core.cql.Ordering
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import java.time.Instant

@Table("messages_by_conversation")
data class MessageByConversationEntity(
    @PrimaryKey val key: MessageByConversationKey,
    @Column("content") val content: String,
    @Column("message_type") val messageType: String,
    @Column("conversation_type") val conversationType: String,
    @Column("file_url") val fileUrl: String? = null,
    @Column("reactions") val reactions: Map<String, Set<String>> = emptyMap(),
    @Column("sender_id") val senderId: String,
    @Column("is_deleted") val isDeleted: Boolean = false,
    @Column("edited_at") val editedAt: Instant? = null,
    @Column("recipient_id") val recipientId: String? = null
)

@PrimaryKeyClass
data class MessageByConversationKey(
    @PrimaryKeyColumn(name = "conversation_id", type = PrimaryKeyType.PARTITIONED) val conversationId: String,
    @PrimaryKeyColumn(
        name = "created_at", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING
    ) val createdAt: Instant,
    @PrimaryKeyColumn(
        name = "message_id", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING
    ) val messageId: String
)

fun Message.toMessageByConversationEntity(): MessageByConversationEntity {
    return MessageByConversationEntity(
        key = MessageByConversationKey(
            conversationId = this.conversationId.value, createdAt = this.createdAt, messageId = this.id.value
        ),
        content = this.content.value,
        messageType = this.messageType.name,
        conversationType = this.conversationType.name,
        fileUrl = this.fileUrl?.value,
        senderId = this.senderId.value,
        isDeleted = this.isDeleted,
        editedAt = this.editedAt,
        recipientId = this.recipientId?.value
    )
}