package com.ethyllium.messageservice.infrastructure.adapter.inbound.websocket.dto

data class WebSocketChatRequest(
    val recipients: String?, // Only one recipient for private chat
    val content: String
)