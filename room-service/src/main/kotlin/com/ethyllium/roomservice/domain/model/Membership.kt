package com.ethyllium.roomservice.domain.model

import java.time.Instant
import java.util.*

data class Membership(
    val userId: UUID, val roomId: UUID, val joinedAt: Instant, val role: RoomRole
)