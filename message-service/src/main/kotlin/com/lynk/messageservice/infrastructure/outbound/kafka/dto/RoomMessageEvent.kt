package com.lynk.messageservice.infrastructure.outbound.kafka.dto

import java.time.Instant
import java.util.UUID

data class RoomMessagePayload(
    val content: String,
    val senderId: UUID,
    val fileUrls: List<String>? = null,
    val roomId: UUID,
    val senderPhoneNumber: String
)

data class RoomMessageEvent(
    val messageId: UUID,
    val payload: RoomMessagePayload,
    val timestamp: Long = Instant.now().toEpochMilli()
)