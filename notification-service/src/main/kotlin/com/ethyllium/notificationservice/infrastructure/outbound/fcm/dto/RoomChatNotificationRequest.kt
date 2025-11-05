package com.ethyllium.notificationservice.infrastructure.outbound.fcm.dto

import com.ethyllium.notificationservice.domain.model.MessageType

data class RoomChatNotificationRequest(
    val topic: String, // a room's topic is same as room's id
    val body: String,
    val senderPhoneNumber: String,
    val messageId: String,
    val messageType: MessageType = MessageType.TEXT,
)