package com.ethyllium.messageservice.infrastructure.adapter.inbound.websocket.dto

data class ReceiverMessage(
    val content: String,
    val sender: String
)
