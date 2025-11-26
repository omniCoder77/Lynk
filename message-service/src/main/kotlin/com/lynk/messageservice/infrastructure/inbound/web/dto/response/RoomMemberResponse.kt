package com.lynk.messageservice.infrastructure.inbound.web.dto.response

import com.lynk.messageservice.domain.model.RoomMember
import com.lynk.messageservice.domain.model.RoomRole
import java.time.Instant
import java.util.UUID

data class RoomMemberResponse(
    val memberId: UUID,
    val displayName: String,
    val role: RoomRole,
    val joinedAt: Instant,
)
fun RoomMember.toResponse(): RoomMemberResponse {
    return RoomMemberResponse(
        memberId = this.memberId,
        displayName = this.displayName,
        role = this.role,
        joinedAt = this.joinedAt,
    )
}