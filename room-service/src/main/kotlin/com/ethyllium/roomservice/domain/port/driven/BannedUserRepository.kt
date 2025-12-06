package com.ethyllium.roomservice.domain.port.driven

import com.ethyllium.roomservice.domain.model.BannedUser
import com.ethyllium.roomservice.domain.model.Room
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

interface BannedUserRepository {
    fun insert(bannedUser: BannedUser): Mono<Boolean>
    fun update(bannedUntil: Instant?, bannedId: UUID): Mono<Boolean>
    fun delete(bannedId: UUID): Mono<Boolean>
    fun select(bannedId: UUID): Mono<BannedUser>
}