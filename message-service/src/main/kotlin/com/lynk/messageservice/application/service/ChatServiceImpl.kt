package com.lynk.messageservice.application.service

import com.lynk.messageservice.domain.port.driven.ChatService
import com.lynk.messageservice.domain.port.driver.ConversationRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class ChatServiceImpl(
    private val conversationRepository: ConversationRepository
) : ChatService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun store(message: String, recipientId: String, senderId: String): Mono<Boolean> {
        return try {
            val recipientUUID = UUID.fromString(recipientId)
            conversationRepository.store(message, senderId, recipientUUID)
        } catch (e: IllegalArgumentException) {
            logger.error("Storing message with id $recipientId is not a valid UUID", e)
            Mono.just(false)
        }
    }
}