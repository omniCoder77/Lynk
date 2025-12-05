package com.ethyllium.userservice.domain.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("room")
data class Room(
    @Id val roomId: UUID,
    val roomName: String,
)