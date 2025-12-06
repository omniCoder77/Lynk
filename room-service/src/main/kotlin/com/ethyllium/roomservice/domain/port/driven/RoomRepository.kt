package com.ethyllium.roomservice.domain.port.driven

import com.ethyllium.roomservice.domain.model.Room
import com.ethyllium.roomservice.domain.model.Visibility
import reactor.core.publisher.Mono
import java.util.UUID

interface RoomRepository {
    fun insert(room: Room): Mono<Boolean>
    fun update(name: String? = null, maxSize: Int? = null, visibility: Visibility? = null, roomId: UUID): Mono<Boolean>
    fun delete(roomId: UUID): Mono<Boolean>
    fun select(roomId: UUID): Mono<Room>
}