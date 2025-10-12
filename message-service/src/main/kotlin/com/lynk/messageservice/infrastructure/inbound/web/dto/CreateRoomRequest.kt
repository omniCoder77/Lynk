package com.lynk.messageservice.infrastructure.inbound.web.dto

import com.lynk.messageservice.domain.model.RoomType
import java.util.UUID

data class CreateRoomRequest(
    val name: String,
    val roomType: RoomType,
    val description: String? = null,
    val avatarUrl: String? = null
)