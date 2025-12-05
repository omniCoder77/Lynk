package com.ethyllium.userservice.infrastructure.outbound.postgres.entity

import com.ethyllium.userservice.domain.model.Room
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table(name = "room")
data class RoomEntity(
    @Id val roomId: UUID,
    val roomName: String
) {
    fun toModel(): Room = Room(roomId, roomName)
}

fun Room.toEntity(): RoomEntity = RoomEntity(this.roomId, this.roomName)