package com.ethyllium.roomservice.domain.port.driver

import com.ethyllium.roomservice.domain.model.Membership
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface MembershipService {
    fun join(roomId: UUID, joinerId: UUID): Mono<Void>
    fun leave(leaverId: UUID, roomId: UUID): Mono<Boolean>
    fun kick(kickerId: UUID, kickedUserId: UUID, roomId: UUID): Mono<Boolean>
    fun getMembers(roomId: UUID): Flux<Membership>
    fun getUserRooms(userId: UUID): Flux<Membership>
}