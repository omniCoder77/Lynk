package com.lynk.messageservice.domain.port.driver

import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.Room
import reactor.core.publisher.Mono
import java.util.*

interface RoomRepository {
    fun create(room: Room): Mono<Room>
    fun getById(roomId: UUID): Mono<Room>
    fun update(room: Room): Mono<Room> // For updating name, avatar, etc.
    fun authorizedDelete(roomId: UUID, ownerId: UUID): Mono<Boolean>
}