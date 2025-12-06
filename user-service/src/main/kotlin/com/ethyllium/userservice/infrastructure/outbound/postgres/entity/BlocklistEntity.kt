package com.ethyllium.userservice.infrastructure.outbound.postgres.entity

import com.ethyllium.userservice.domain.model.Blocklist
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table(name = "blocklist")
data class BlocklistEntity(
    @Id val blocklistId: UUID, // blocklistId is indexed
    val userId: UUID,
    val blockedUserId: UUID,
    val blockedAt: Instant
) {
    fun toModel() = Blocklist(blocklistId, userId, blockedUserId, blockedAt)
}

fun Blocklist.toEntity() = BlocklistEntity(blocklistId, userId, blockedUserId, blockedAt)