package com.lynk.messageservice.infrastructure.inbound.web.dto.response

import java.util.UUID

data class RoomResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val avatarUrl: String?,
    val members: List<RoomMemberResponse>
)
