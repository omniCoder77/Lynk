package com.lynk.messageservice.application.service

import com.lynk.messageservice.domain.port.driven.ConversationService
import com.lynk.messageservice.domain.port.driver.ConversationByUserRepository
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.ConversationByUser
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class ConversationServiceImpl(
    private val conversationByUserRepository: ConversationByUserRepository
): ConversationService {
    override fun getConversations(userId: String): Flux<ConversationByUser> {
        return conversationByUserRepository.get(userId)
    }

    override fun getConversation(userId: String, recipientId: String): Mono<ConversationByUser> {
        return conversationByUserRepository.get(userId, recipientId)
    }
}