package com.lynk.messageservice.infrastructure.inbound.web.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class CreateRoomRequest(
    @field:NotBlank(message = "Room name cannot be blank.")
    @field:Size(min = 1, max = 100, message = "Room name must be between 1 and 100 characters.")
    val name: String,
    @field:Size(max = 500, message = "Room description cannot exceed 500 characters.")
    val description: String?,
    val initialMemberIds: List<UUID>
)