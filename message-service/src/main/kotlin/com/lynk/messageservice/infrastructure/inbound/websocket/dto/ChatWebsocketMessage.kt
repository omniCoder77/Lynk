package com.lynk.messageservice.infrastructure.inbound.websocket.dto

import java.time.Instant

data class ChatWebsocketMessage(
    val recipientId: String,
    val replyToMessageId: String? = null,
    val content: String,
    val timestamp: Instant = Instant.now(),
)