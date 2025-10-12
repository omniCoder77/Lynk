package com.lynk.messageservice.infrastructure.inbound.web.dto

import com.lynk.messageservice.domain.model.RoomType

data class UpdateRoomRequest(
    val name: String? = null,
    val description: String? = null,
    val avatarUrl: String? = null,
    val roomType: RoomType? = null
)