package com.ethyllium.notificationservice.infrastructure.outbound.fcm.dto

import com.ethyllium.notificationservice.domain.model.MessageType

data class ConversationChatNotificationRequest(
    val token: String,
    val body: String,
    val conversationId: String,
    val senderId: String,
    val messageId: String,
    val messageType: MessageType = MessageType.TEXT,
    val senderAvatar: String? = null,
)