package com.ethyllium.roomservice.domain.port.driven

import com.ethyllium.roomservice.domain.model.Membership
import com.ethyllium.roomservice.domain.model.Room
import com.ethyllium.roomservice.domain.model.RoomRole
import reactor.core.publisher.Mono
import java.util.*

interface MembershipRepository {
    fun insert(membership: Membership): Mono<Boolean>
    fun update(role: RoomRole, membershipId: UUID): Mono<Boolean>
    fun delete(membershipId: UUID): Mono<Boolean>
    fun select(membershipId: UUID): Mono<Membership>
}