package com.ethyllium.notificationservice.infrastructure.input.kafka.dto

data class NewMessage(
    val senderId: String,
    val receiverId: String,
    val title: String,
    val body: String
)