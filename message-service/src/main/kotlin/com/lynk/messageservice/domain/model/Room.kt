package com.lynk.messageservice.domain.model

import java.util.UUID

data class Room(
    val id: UUID,
    val name: String,
)