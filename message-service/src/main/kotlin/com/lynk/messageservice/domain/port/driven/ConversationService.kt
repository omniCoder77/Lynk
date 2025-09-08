package com.lynk.messageservice.domain.port.driven

import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.ConversationByUser
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ConversationService {
    fun getConversations(userId: String): Flux<ConversationByUser>
    fun getConversation(userId: String, recipientId: String): Mono<ConversationByUser>
}