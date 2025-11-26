package com.lynk.messageservice.infrastructure.inbound.websocket.dto

import java.time.Instant

data class RoomWebsocketMessage(
    val roomId: String,
    val senderId: String,
    val replyToMessageId: String? = null,
    val content: String,
    val timestamp: Instant = Instant.now(),
)