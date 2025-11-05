package com.lynk.messageservice.application.service

import com.lynk.messageservice.domain.model.Conversation
import com.lynk.messageservice.domain.port.driven.ConversationService
import com.lynk.messageservice.domain.port.driver.ConversationRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import java.time.Instant

@Component
class ConversationServiceImpl(
    private val conversationRepository: ConversationRepository
) : ConversationService {
    override fun getConversations(userId: String, start: Instant, end: Instant): Flux<Conversation> {
        return conversationRepository.get(userId, start, end)
    }
}