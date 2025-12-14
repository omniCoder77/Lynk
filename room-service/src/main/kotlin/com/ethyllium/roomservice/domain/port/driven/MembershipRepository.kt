package com.ethyllium.roomservice.domain.port.driven

import com.ethyllium.roomservice.domain.model.Membership
import com.ethyllium.roomservice.domain.model.RoomRole
import reactor.core.publisher.Mono
import java.util.*

interface MembershipRepository {
    fun insert(membership: Membership): Mono<Void>
    fun update(role: RoomRole, membershipId: UUID): Mono<Long>
    fun delete(membershipId: UUID, roomId: UUID?, role: RoomRole?): Mono<Long>
    fun select(membershipId: UUID, roomId: UUID? = null, roles: Array<RoomRole> = emptyArray()): Mono<Membership>
}