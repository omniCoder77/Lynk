package com.ethyllium.messageservice.domain.model

data class Message(
    val messageId: String,
    val senderId: String,
    val receiverId: String,
    val timestamp: Long,
    val content: String,
    val file: String?,
    val referencedUserIds: List<String>
)