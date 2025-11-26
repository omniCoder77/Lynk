package com.lynk.messageservice.domain.port.driver

import reactor.core.publisher.Mono
import java.time.Instant
import java.util.UUID

interface MessageReactionRepository {
    fun addReaction(roomId: UUID, messageId: UUID, memberId: UUID, emoji: String, reactedAt: Instant = Instant.now()): Mono<Boolean>
    fun deleteReaction(roomId: UUID, messageId: UUID, memberId: UUID): Mono<Boolean>
}