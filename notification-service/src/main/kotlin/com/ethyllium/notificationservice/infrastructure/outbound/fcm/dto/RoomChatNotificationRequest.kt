package com.ethyllium.notificationservice.infrastructure.outbound.fcm.dto

import com.ethyllium.notificationservice.domain.model.MessageType

data class RoomChatNotificationRequest(
    val topic: String, // a room's topic is same as room's Id
    val body: String,
    val conversationId: String,
    val senderId: String,
    val messageId: String,
    val messageType: MessageType = MessageType.TEXT,
    val senderAvatar: String? = null,
)