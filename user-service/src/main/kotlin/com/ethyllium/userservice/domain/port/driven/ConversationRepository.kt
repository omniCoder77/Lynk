package com.ethyllium.userservice.domain.port.driven

import com.ethyllium.userservice.domain.model.Conversation
import com.ethyllium.userservice.infrastructure.outbound.postgres.entity.ConversationEntity
import reactor.core.publisher.Mono
import java.util.UUID

interface ConversationRepository {
    fun store(conversation: Conversation): Mono<ConversationEntity>
    fun delete(conversationId: UUID): Mono<Boolean>
    fun select(conversationId: UUID): Mono<Conversation>
    fun exists(conversationId: UUID): Mono<Boolean>
}