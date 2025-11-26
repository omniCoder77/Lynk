package com.lynk.messageservice.domain.model

import java.time.Instant
import java.util.UUID

data class Conversation(
    val userId: UUID,
    val recipientId: UUID,
    val lastActivityTimestamp: Instant,
)
