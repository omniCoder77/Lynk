package com.lynk.messageservice.infrastructure.inbound.websocket.dto

data class Conversation(
    val recipientPhoneNumber: String,
    val message: String
)
