package com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.entity

import com.ethyllium.messageservice.domain.model.*
import org.springframework.data.cassandra.core.cql.Ordering
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.*
import java.time.Instant

@Table("user_messages")
data class MessageByUserEntity(
    @PrimaryKey val key: MessageByUserKey,
    @Column("content") val content: String,
    @Column("message_type") val message_type: String,
    @Column("conversation_type") val conversation_type: String,
    @Column("conversation_id") val conversation_id: String,
    @Column("other_user_id") val recipient_id: String? = null,
    @Column("file_url") val file_url: String? = null,
    @Column("edited_at") val edited_at: Instant? = null,
    @Column("is_deleted") val is_deleted: Boolean = false
)

@PrimaryKeyClass
data class MessageByUserKey(
    @PrimaryKeyColumn(name = "bucket", type = PrimaryKeyType.PARTITIONED) val bucket: Int,
    @PrimaryKeyColumn(name = "sender_id", type = PrimaryKeyType.CLUSTERED) val senderId: String,
    @PrimaryKeyColumn(
        name = "created_at", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING
    ) val createdAt: Instant,
    @PrimaryKeyColumn(name = "message_id", type = PrimaryKeyType.CLUSTERED) val messageId: String
)

fun Message.toMessageByUserEntity(bucket: Int): MessageByUserEntity {
    return MessageByUserEntity(
        key = MessageByUserKey(
            bucket = bucket, senderId = this.senderId.value, createdAt = this.createdAt, messageId = this.id.value
        ),
        content = this.content.value,
        message_type = this.messageType.name,
        conversation_type = this.conversationType.name,
        conversation_id = this.conversationId.value,
        recipient_id = this.recipientId?.value,
        file_url = this.fileUrl?.value,
        edited_at = this.editedAt,
        is_deleted = this.isDeleted
    )
}

fun MessageByUserEntity.toDomainMessage(): Message {
    return Message(
        id = MessageId(this.key.messageId),
        conversationId = ConversationId(this.conversation_id),
        senderId = UserId(this.key.senderId),
        content = MessageContent(this.content),
        messageType = MessageType.valueOf(this.message_type),
        conversationType = ConversationType.valueOf(this.conversation_type),
        recipientId = this.recipient_id?.let { UserId(it) },
        fileUrl = this.file_url?.let { FileUrl(it) },
        createdAt = this.key.createdAt,
        editedAt = this.edited_at,
        isDeleted = this.is_deleted
    )
}