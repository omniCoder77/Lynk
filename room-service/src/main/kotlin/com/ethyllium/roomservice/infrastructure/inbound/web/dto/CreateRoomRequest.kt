package com.ethyllium.roomservice.infrastructure.inbound.web.dto

import com.ethyllium.roomservice.domain.model.Visibility
import com.ethyllium.roomservice.infrastructure.util.RoomUtils
import java.util.UUID

data class CreateRoomRequest(
    val idempotencyKey: UUID,
    val roomName: String,
    val maxSize: Int = RoomUtils.DEFAULT_ROOM_CAPACITY,
    val visibility: Visibility = Visibility.PUBLIC
)
