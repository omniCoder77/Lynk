package com.ethyllium.messageservice.application.dto

import com.ethyllium.messageservice.domain.model.ConversationType
import com.ethyllium.messageservice.domain.model.MessageType

data class MessageRequest(
    val recipientId: String,
    val conversationId: String,
    val conversationType: ConversationType,
    val content: String,
    val messageType: MessageType,
    val fileUrl: String? = null
)
