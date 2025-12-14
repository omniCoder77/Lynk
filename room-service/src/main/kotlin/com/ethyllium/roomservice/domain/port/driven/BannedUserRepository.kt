package com.ethyllium.roomservice.domain.port.driven

import com.ethyllium.roomservice.domain.model.BannedUser
import com.ethyllium.roomservice.domain.model.Room
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

interface BannedUserRepository {
    fun insert(bannedUser: BannedUser): Mono<Void>
    fun update(bannedUntil: Instant?, bannedId: UUID): Mono<Long>
    fun delete(bannedId: UUID): Mono<Long>
    fun select(bannedId: UUID): Mono<BannedUser>
}