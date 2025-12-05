package com.ethyllium.userservice.infrastructure.outbound.postgres.entity

import com.ethyllium.userservice.domain.model.Conversation
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table(name = "conversation")
data class ConversationEntity(
    @Id val conversationId: UUID,
    val senderId: UUID,
    val recipientId: UUID,
    val isBlocked: Boolean
) {
    fun toConversation(): Conversation = Conversation(conversationId, senderId, recipientId, isBlocked)
}

fun Conversation.toEntity(): ConversationEntity = ConversationEntity(conversationId, senderId, recipientId, isBlocked)