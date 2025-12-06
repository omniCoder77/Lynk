package com.ethyllium.userservice.domain.port.driver

import com.ethyllium.userservice.domain.model.Conversation
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

interface ConversationService {
    fun block(userId: UUID, blockedUserUuid: UUID): Mono<UUID>
    fun unblock(userId: UUID, blockedUserUuid: UUID): Mono<Boolean>
    fun createConversationForUser(user1Id: UUID, user2Id: UUID): Mono<UUID>
    fun getConversationsForUser(userId: UUID): Flux<Conversation>
    fun delete(userId: UUID, recipientId: UUID): Mono<Boolean>
}