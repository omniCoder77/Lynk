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
    @Column("message_type") val messageType: String,
    @Column("conversation_type") val conversationType: String,
    @Column("conversation_id") val conversationId: String,
    @Column("other_user_id") val recipientId: String? = null,
    @Column("file_url") val fileUrl: String? = null,
    @Column("edited_at") val editedAt: Instant? = null,
    @Column("is_deleted") val isDeleted: Boolean = false
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
        messageType = this.messageType.name,
        conversationType = this.conversationType.name,
        conversationId = this.conversationId.value,
        recipientId = this.recipientId?.value,
        fileUrl = this.fileUrl?.value,
        editedAt = this.editedAt,
        isDeleted = this.isDeleted
    )
}

fun MessageByUserEntity.toDomainMessage(): Message {
    return Message(
        id = MessageId(this.key.messageId),
        conversationId = ConversationId(this.conversationId),
        senderId = UserId(this.key.senderId),
        content = MessageContent(this.content),
        messageType = MessageType.valueOf(this.messageType),
        conversationType = ConversationType.valueOf(this.conversationType),
        recipientId = this.recipientId?.let { UserId(it) },
        fileUrl = this.fileUrl?.let { FileUrl(it) },
        createdAt = this.key.createdAt,
        editedAt = this.editedAt,
        isDeleted = this.isDeleted
    )
}