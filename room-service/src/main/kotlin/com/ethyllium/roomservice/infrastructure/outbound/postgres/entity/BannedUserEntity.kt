package com.ethyllium.roomservice.infrastructure.outbound.postgres.entity

import com.ethyllium.roomservice.domain.model.BannedUser
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table(name = "banned_users")
data class BannedUserEntity(
    @Id val bannedId: UUID, // Created using UUIDUtils.merge()
    val userId: UUID,
    val roomId: UUID,
    val reason: String,
    val bannedAt: Instant,
    val bannedUntil: Instant? // nullable; null means permanent ban. Entity is removed when ban is lifted or expired
) {
    fun toDomain() = BannedUser(
        banId = bannedId,
        userId = userId,
        roomId = roomId,
        reason = reason,
        bannedAt = bannedAt,
        bannedUntil = bannedUntil
    )
}

fun BannedUser.toEntity() = BannedUserEntity(
    bannedId = banId,
    userId = userId,
    roomId = roomId,
    reason = reason,
    bannedAt = bannedAt,
    bannedUntil = bannedUntil
)