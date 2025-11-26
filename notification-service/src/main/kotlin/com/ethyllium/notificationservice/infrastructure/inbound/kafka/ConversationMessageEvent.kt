package com.ethyllium.notificationservice.infrastructure.inbound.kafka

import java.time.Instant
import java.util.UUID

data class ConversationMessageEvent(
    val messageId: UUID,
    val senderId: UUID = UUID.randomUUID(),
    val payload: ConversationMessagePayload,
    val timestamp: Instant = Instant.now()
)

data class ConversationMessagePayload(
    val content: String,
    val fileUrls: List<String>? = null,
    val recipientId: UUID,
    val phoneNumber: String
)