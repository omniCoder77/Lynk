package com.ethyllium.roomservice.domain.port.driver

import reactor.core.publisher.Mono
import java.time.Instant
import java.util.UUID

interface BannedService {
    fun ban(bannerId: UUID, roomId: UUID, bannedUserId: UUID, reason: String? = null, bannedUntil: Instant? = null): Mono<Void>
    fun unban(unbannerId: UUID, roomId: UUID, bannedUserId: UUID): Mono<Boolean>
}