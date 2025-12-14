package com.ethyllium.roomservice.domain.port.driver

import com.ethyllium.roomservice.domain.model.Room
import com.ethyllium.roomservice.domain.model.Visibility
import reactor.core.publisher.Mono
import java.util.UUID

interface RoomService {
    fun create(room: Room, creatorId: UUID): Mono<Boolean>
    fun update(updaterId: UUID, roomName: String?, roomId: UUID, maxSize: Int?, visibility: Visibility?): Mono<Boolean>
    fun delete(deleterId: UUID, roomId: UUID): Mono<Boolean>
}