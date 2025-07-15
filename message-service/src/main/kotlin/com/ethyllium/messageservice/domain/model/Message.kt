package com.ethyllium.messageservice.domain.model

import java.time.Instant
import java.util.*

data class MessageId(val value: String = UUID.randomUUID().toString())
data class ConversationId(val value: String = UUID.randomUUID().toString())
data class UserId(val value: String)
data class MessageContent(val value: String)
data class FileUrl(val value: String)

enum class MessageType {
    TEXT, AUDIO, VIDEO, IMAGE, FILE, SYSTEM
}

enum class ConversationType {
    GROUP, PRIVATE
}

data class Message(
    val id: MessageId,
    val conversationId: ConversationId,
    val senderId: UserId,
    val content: MessageContent,
    val messageType: MessageType,
    val conversationType: ConversationType,
    val recipientId: UserId? = null,
    val fileUrl: FileUrl? = null,
    val createdAt: Instant,
    val editedAt: Instant? = null,
    val isDeleted: Boolean = false
)

data class Conversation(
    val conversationId: ConversationId,
    val type: ConversationType,
    val lastMessageId: MessageId,
    val lastMessageReadId: MessageId? = null,
    val lastMessageSentId: MessageId? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val name: String? = null,
    val iconUrl: String? = null,
)

data class ConversationMember(
    val conversationId: ConversationId,
    val userId: UserId,
    val role: MemberRole,
    val joinedAt: Instant = Instant.now(),
    val isArchived: Boolean = false,
)

enum class MemberRole {
    ADMIN, MEMBER
}

enum class MessageStatus {
    SENT, DELIVERED, SEEN
}