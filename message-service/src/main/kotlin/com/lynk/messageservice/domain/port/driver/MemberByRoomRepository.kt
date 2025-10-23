package com.lynk.messageservice.domain.port.driver

import com.lynk.messageservice.domain.model.RoomMember
import com.lynk.messageservice.domain.model.RoomRole
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.MemberByRoom
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface MemberByRoomRepository {
    fun createMemberByRoom(roomId: UUID, memberId: UUID, role: RoomRole, displayName: String, description: String? = null): Mono<Boolean>
    fun updateRoom(description: String?, avatarUrl: String?, displayName: String?, roomId: UUID, updater: UUID): Flux<UUID>
    fun getMembersByRoomId(roomId: UUID): Flux<RoomMember>
    fun getMemberById(inviterId: UUID, roomId: UUID): Mono<MemberByRoom>
    fun deleteMemberByRoomId(roomId: UUID, deleteId: UUID): Mono<Boolean>
}