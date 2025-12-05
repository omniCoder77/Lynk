package com.ethyllium.userservice.domain.port.driven

import com.ethyllium.userservice.domain.model.Room
import reactor.core.publisher.Mono
import java.util.UUID

interface RoomRepository {
    fun store(room: Room): Mono<UUID>
    fun delete(roomId: UUID): Mono<Boolean>
    fun get(roomId: UUID): Mono<Room>
}