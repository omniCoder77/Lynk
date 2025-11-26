package com.lynk.messageservice.domain.port.driver

import com.lynk.messageservice.domain.model.Conversation
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.UUID

interface ConversationRepository {
    fun get(userId: String, start: Instant, end: Instant): Flux<Conversation>
    fun store(message: String, senderId: UUID, recipientId: UUID): Mono<Boolean>
    fun delete(userId: String, start: Instant, end: Instant): Mono<Boolean>
    fun exists(userId: UUID, recipientId: UUID): Mono<Boolean>
    fun insert(user1: UUID, user2: UUID): Mono<Boolean>
}