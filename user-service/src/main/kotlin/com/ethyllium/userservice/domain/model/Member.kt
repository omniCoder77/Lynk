package com.ethyllium.userservice.domain.model

import java.time.Instant
import java.util.UUID

data class Member(
    val memberId: UUID, // same as userId
    val joinedAt: Instant,
    val role: MemberRole,
    val isAllowedToMessage: Boolean,
    val isAllowedToSendMedia: Boolean
)
