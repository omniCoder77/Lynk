package com.ethyllium.roomservice.domain.port.driven

import com.ethyllium.roomservice.domain.model.Room
import com.ethyllium.roomservice.domain.model.Visibility
import reactor.core.publisher.Mono
import java.util.UUID

interface RoomRepository {
    fun insert(room: Room): Mono<Void>
    fun update(name: String? = null, maxSize: Int? = null, visibility: Visibility? = null, roomId: UUID): Mono<Long>
    fun delete(roomId: UUID): Mono<Long>
    fun select(roomId: UUID): Mono<Room>
}