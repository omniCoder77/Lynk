package com.ethyllium.roomservice.infrastructure.outbound.postgres.entity

import com.ethyllium.roomservice.domain.model.Room
import com.ethyllium.roomservice.domain.model.Visibility
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("room")
data class RoomEntity(
    @Id val roomId: UUID,
    val name: String,
    val maxSize: Int,
    val visibility: Visibility
) {
    fun toDomain() = Room(
        roomId = roomId,
        name = name,
        maxSize = maxSize,
        visibility = visibility
    )
}

fun Room.toEntity() = RoomEntity(
    roomId = roomId,
    name = name,
    maxSize = maxSize,
    visibility = visibility
)