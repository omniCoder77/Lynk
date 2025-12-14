package com.ethyllium.roomservice.infrastructure.inbound.web.dto

import com.ethyllium.roomservice.domain.model.Visibility
import java.util.UUID

data class UpdateRoomRequest(
    val roomId: UUID,
    val roomName: String? = null,
    val maxSize: Int? = null,
    val visibility: Visibility? = null
)
