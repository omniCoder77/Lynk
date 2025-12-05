package com.ethyllium.userservice.infrastructure.outbound.postgres.entity

import com.ethyllium.userservice.domain.model.Member
import com.ethyllium.userservice.domain.model.MemberRole
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table(name = "member")
data class MemberEntity(
    @Id val memberId: UUID, // same as userId
    val joinedAt: Instant,
    val role: MemberRole,
    val isAllowedToMessage: Boolean,
    val isAllowedToSendMedia: Boolean
) {
    fun toModel(): Member = Member(memberId, joinedAt, role, isAllowedToMessage, isAllowedToSendMedia)
}

fun Member.toEntity() = MemberEntity(memberId, joinedAt, role, isAllowedToMessage, isAllowedToSendMedia)