package com.ethyllium.userservice.infrastructure.outbound.postgres.entity

import com.ethyllium.userservice.domain.model.Conversation
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table(name = "conversation")
data class ConversationEntity(
    @Id val conversationId: UUID,
    val senderId: UUID,
    val recipientId: UUID,
) {
    fun toConversation(): Conversation = Conversation(conversationId, senderId, recipientId)
}

fun Conversation.toEntity(): ConversationEntity = ConversationEntity(conversationId, senderId, recipientId)