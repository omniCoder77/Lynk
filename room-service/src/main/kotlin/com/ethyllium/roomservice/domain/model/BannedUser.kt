package com.ethyllium.roomservice.domain.model

import java.time.Instant
import java.util.*

data class BannedUser(
    val banId: UUID, // Created using UUIDUtils.merge()
    val userId: UUID,
    val roomId: UUID,
    val reason: String,
    val bannedAt: Instant,
    val bannedUntil: Instant? // nullable; null means permanent ban. Entity is removed when ban is lifted or expired
)