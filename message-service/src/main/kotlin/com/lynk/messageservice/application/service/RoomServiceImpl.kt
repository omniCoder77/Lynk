package com.lynk.messageservice.application.service

import com.lynk.messageservice.domain.model.RoomType
import com.lynk.messageservice.domain.port.driven.RoomService
import com.lynk.messageservice.domain.port.driver.RoomRepository
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.Room
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.UUID

@Component
class RoomServiceImpl(
    private val roomRepository: RoomRepository
) : RoomService {
    override fun createRoom(
        name: String,
        creatorId: UUID,
        roomType: RoomType,
        description: String?,
        avatarUrl: String?
    ): Mono<Room> {
        val newRoom = Room(
            name = name,
            creatorId = creatorId,
            roomType = roomType.name,
            description = description,
            avatarUrl = avatarUrl,
            createdAt = Instant.now(),
            lastActivityTimestamp = Instant.now()
        )
        return roomRepository.create(newRoom)
    }

    override fun getRoomDetails(roomId: UUID): Mono<Room> {
        return roomRepository.getById(roomId)
    }

    override fun updateRoom(
        roomId: UUID,
        name: String?,
        description: String?,
        avatarUrl: String?,
        roomType: RoomType?
    ): Mono<Room> {
        return roomRepository.getById(roomId)
            .flatMap { existingRoom ->
                val updatedRoom = existingRoom.copy(
                    name = name ?: existingRoom.name,
                    description = description ?: existingRoom.description,
                    avatarUrl = avatarUrl ?: existingRoom.avatarUrl,
                    roomType = (roomType?.name ?: existingRoom.roomType),
                    lastActivityTimestamp = Instant.now()
                )
                roomRepository.update(updatedRoom)
            }
    }

    override fun deleteRoom(roomId: UUID, callingUserId: UUID): Mono<Boolean> {
        return roomRepository.getById(roomId)
            .flatMap { room ->
                if (room.creatorId == callingUserId) {
                    roomRepository.authorizedDelete(roomId, callingUserId)
                } else {
                    Mono.just(false)
                }
            }
            .defaultIfEmpty(false)
    }
}