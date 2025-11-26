package com.lynk.messageservice.domain.model

import java.io.Serializable
import java.time.Instant
import java.util.UUID

data class RoomMember(
    val displayName: String,
    val role: RoomRole,
    val joinedAt: Instant,
    val memberId: UUID,
    val roomId: UUID,
    val description: String?,
): Serializable