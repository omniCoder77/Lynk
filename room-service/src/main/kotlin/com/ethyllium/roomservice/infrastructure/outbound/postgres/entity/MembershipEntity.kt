package com.ethyllium.roomservice.infrastructure.outbound.postgres.entity

import com.ethyllium.roomservice.domain.model.Membership
import com.ethyllium.roomservice.domain.model.RoomRole
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("membership")
data class MembershipEntity(
    @Id val userId: UUID,
    val roomId: UUID,
    val joinedAt: Instant,
    val role: RoomRole
) {
    fun toDomain() = Membership(
        userId = userId,
        roomId = roomId,
        joinedAt = joinedAt,
        role = role
    )
}

fun Membership.toEntity() = MembershipEntity(
    userId = userId,
    roomId = roomId,
    joinedAt = joinedAt,
    role = role
)