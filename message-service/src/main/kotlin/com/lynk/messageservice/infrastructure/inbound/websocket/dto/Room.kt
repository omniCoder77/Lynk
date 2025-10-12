package com.lynk.messageservice.infrastructure.inbound.websocket.dto

data class Room(
    val roomId: String,
    val text: String
)