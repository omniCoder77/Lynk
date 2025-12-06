package com.ethyllium.userservice.domain.model

import java.util.UUID

data class Conversation(
    val conversationId: UUID,
    val senderId: UUID,
    val recipientId: UUID
)
