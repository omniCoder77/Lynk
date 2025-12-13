package com.ethyllium.roomservice.domain.port.driver

import com.ethyllium.roomservice.domain.model.Room
import reactor.core.publisher.Mono
import java.util.UUID

interface RoomService {
    fun create(room: Room, creatorId: UUID): Mono<Boolean>
}