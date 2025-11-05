package com.lynk.messageservice.domain.model

import java.time.Instant
import java.util.UUID

data class Message(val conversationId: UUID, val messageTimestamp: Instant, val senderId: UUID, val content: String)