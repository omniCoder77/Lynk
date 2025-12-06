package com.ethyllium.userservice.domain.port.driven

import com.ethyllium.userservice.domain.model.Conversation
import com.ethyllium.userservice.infrastructure.outbound.postgres.entity.ConversationEntity
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface ConversationRepository {
    fun insert(conversation: Conversation): Mono<UUID>
    fun delete(conversationId: UUID): Mono<Boolean>
    fun select(conversationId: UUID): Mono<Conversation>
    fun exists(conversationId: UUID): Mono<Boolean>
    fun findByUserId(userId: UUID): Flux<Conversation>
}