package com.lynk.messageservice.domain.model

import java.time.Instant
import java.util.UUID

data class Reaction(
    val emoji: String,
    val memberId: UUID,
    val reactedAt: Instant
)