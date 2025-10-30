package com.lynk.messageservice.domain.port.driver

import com.lynk.messageservice.domain.model.Room
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface RoomByMemberRepository {
    fun createRoomByMember(memberId: UUID, roomId: UUID, name: String, avatarExtension: String? = null): Mono<UUID>
    fun updateRoomByMember(memberId: UUID, roomId: UUID, lastMessagePreview: String? = null, lastMessenger: String? = null, avatarExtension: String? = null, name: String? = null): Mono<Boolean>
    fun getRooms(memberId: UUID): Flux<Room>
}