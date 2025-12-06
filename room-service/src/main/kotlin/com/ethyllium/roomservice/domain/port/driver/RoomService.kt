package com.ethyllium.roomservice.domain.port.driver

import com.ethyllium.roomservice.domain.model.Room
import reactor.core.publisher.Mono

interface RoomService {
    fun create(room: Room): Mono<Boolean>
}