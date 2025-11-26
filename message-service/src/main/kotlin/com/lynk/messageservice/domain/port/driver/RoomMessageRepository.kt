package com.lynk.messageservice.domain.port.driver

import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.RoomMessage
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

interface RoomMessageRepository {
    fun saveMessage(roomId: UUID, content: String, senderId: UUID, messageId: UUID, replyToMessageId: UUID? = null, timestamp: Instant = Instant.now()): Mono<Boolean>
    fun getMessagesByRoomId(roomId: UUID, start: Instant, end: Instant): Flux<RoomMessage>
}