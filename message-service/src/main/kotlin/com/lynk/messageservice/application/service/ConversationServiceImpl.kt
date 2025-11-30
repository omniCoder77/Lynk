package com.lynk.messageservice.application.service

import com.lynk.messageservice.domain.model.Conversation
import com.lynk.messageservice.domain.port.driven.ConversationService
import com.lynk.messageservice.domain.port.driven.EventPublisher
import com.lynk.messageservice.domain.port.driver.ConversationRepository
import com.lynk.messageservice.infrastructure.outbound.kafka.dto.ConversationMessageEvent
import com.lynk.messageservice.infrastructure.outbound.kafka.dto.ConversationMessagePayload
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Component
class ConversationServiceImpl(
    private val conversationRepository: ConversationRepository, private val eventPublisher: EventPublisher
) : ConversationService {
    override fun getConversations(userId: String, recipientId: String): Flux<Conversation> {
        return conversationRepository.get(userId, recipientId)
    }

    override fun sendMessage(
        userId: String, recipientId: String, content: String, replyToMessageId: String?, phoneNumber: String
    ): Mono<Boolean> {
        val userId = UUID.fromString(userId)
        val recipientId = UUID.fromString(recipientId)
        return conversationRepository.exists(userId, recipientId).flatMap { exists ->
            if (!exists) {
                conversationRepository.insert(userId, recipientId)
            } else {
                conversationRepository.store(message = content, senderId = userId, recipientId = recipientId)
                    .flatMap { created ->
                        if (created) {
                            val conversationMessageEvent = ConversationMessageEvent(
                                senderId = userId,
                                payload = ConversationMessagePayload(
                                    content = content, recipientId = recipientId, phoneNumber = phoneNumber
                                ),
                            )
                            Mono.fromRunnable { eventPublisher.publish(conversationMessageEvent) }
                        } else {
                            Mono.error(RuntimeException("Message already exists"))
                        }
                    }
            }
        }
    }
}