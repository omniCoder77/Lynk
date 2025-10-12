package com.lynk.messageservice.domain.port.driven

import com.lynk.messageservice.domain.model.RoomType
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.Room
import reactor.core.publisher.Mono
import java.util.UUID

interface RoomService {
    fun createRoom(name: String, creatorId: UUID, roomType: RoomType, description: String? = null, avatarUrl: String? = null): Mono<Room>
    fun getRoomDetails(roomId: UUID): Mono<Room>
    fun updateRoom(roomId: UUID, name: String?, description: String?, avatarUrl: String?, roomType: RoomType?): Mono<Room>
    fun deleteRoom(roomId: UUID, callingUserId: UUID): Mono<Boolean>
}