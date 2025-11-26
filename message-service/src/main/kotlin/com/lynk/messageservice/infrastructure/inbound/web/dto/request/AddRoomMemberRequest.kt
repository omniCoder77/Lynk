package com.lynk.messageservice.infrastructure.inbound.web.dto.request

import com.lynk.messageservice.domain.model.RoomRole
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class AddRoomMemberRequest(

    @field:NotNull(message = "memberId cannot be null.")
    val memberId: UUID,
    val role: RoomRole = RoomRole.MEMBER
)