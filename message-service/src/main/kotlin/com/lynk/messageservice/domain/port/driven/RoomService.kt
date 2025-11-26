package com.lynk.messageservice.domain.port.driven

import com.lynk.messageservice.domain.model.Room
import com.lynk.messageservice.domain.model.RoomMember
import com.lynk.messageservice.domain.model.RoomRole
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.RoomMessage
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.UUID

interface RoomService {
    fun createRoom(name: String, description: String?, creatorId: UUID, initialMemberIds: List<UUID>): Mono<UUID>
    fun updateRoomDetails(roomId: UUID, name: String?, description: String?, avatarUrl: String?, updater: String): Mono<Boolean>
    fun addMemberToRoom(roomId: UUID, memberId: UUID, role: RoomRole, inviterId: UUID): Mono<Boolean>
    fun getRoomMembers(roomId: UUID): Flux<RoomMember>
    fun sendMessage(
        roomId: UUID,
        senderId: UUID,
        content: String,
        replyToMessageId: UUID?,
        timestamp: Instant,
        phoneNumber: String
    ): Mono<Boolean>
    fun getMessages(roomId: UUID, start: Instant, end: Instant): Flux<RoomMessage>
    fun getRooms(memberId: UUID): Flux<Room>
}