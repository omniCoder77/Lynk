package com.lynk.messageservice.domain.port.driver

import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.ConversationByUser
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ConversationByUserRepository {
    fun get(userId: String): Flux<ConversationByUser>
    fun get(senderId: String, recipientId: String): Mono<ConversationByUser>
}