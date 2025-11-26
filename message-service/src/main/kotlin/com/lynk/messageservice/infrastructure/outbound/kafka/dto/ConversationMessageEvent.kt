package com.lynk.messageservice.infrastructure.outbound.kafka.dto

import java.time.Instant
import java.util.UUID

data class ConversationMessageEvent(
    val senderId: UUID = UUID.randomUUID(),
    val payload: ConversationMessagePayload,
    val timestamp: Long = Instant.now().toEpochMilli()
)

data class ConversationMessagePayload(
    val content: String,
    val fileUrls: List<String>? = null,
    val recipientId: UUID,
    val phoneNumber: String
)