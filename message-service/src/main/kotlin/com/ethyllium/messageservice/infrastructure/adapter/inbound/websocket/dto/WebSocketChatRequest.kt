package com.ethyllium.messageservice.infrastructure.adapter.inbound.websocket.dto

data class SenderMessage(
    val content: String,
    val recipient: List<String>
)