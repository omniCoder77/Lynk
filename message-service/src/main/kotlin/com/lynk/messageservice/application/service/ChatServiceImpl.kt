package com.lynk.messageservice.application.service

import com.lynk.messageservice.domain.port.driven.ChatService
import com.lynk.messageservice.domain.port.driver.ConversationByUserRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class ChatServiceImpl(
    private val conversationByUserRepository: ConversationByUserRepository
) : ChatService {
    override fun store(message: String, recipientId: String, senderId: String): Mono<Boolean> {
        return conversationByUserRepository.store(message, senderId, recipientId)
    }
}