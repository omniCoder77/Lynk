package com.ethyllium.messageservice.application.service

import com.ethyllium.messageservice.application.port.outbound.MessageInteractionService
import com.ethyllium.messageservice.domain.exception.MessageNotFoundException
import com.ethyllium.messageservice.domain.port.outbound.MessageByUserRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class MessageInteractionServiceImpl(private val messageByUserRepository: MessageByUserRepository) :
    MessageInteractionService {
    override fun addReaction(
        userId: String, messageId: String, emoji: String, createAt: Long
    ): Mono<Boolean> {
        return messageByUserRepository.findById(userId, createAt, messageId).flatMap {
            messageByUserRepository.addReaction(userId, messageId, emoji, createAt)
        }
    }

    override fun removeReaction(
        userId: String, messageId: String, emoji: String, createAt: Long
    ): Mono<Boolean> {
        return messageByUserRepository.findById(userId, createAt, messageId).flatMap {
            messageByUserRepository.removeReaction(
                userId, messageId, emoji, createAt
            )
        }
    }

    override fun markMessagesRead(
        userId: String, messageId: String, createAt: Long
    ): Mono<Boolean> {
        return messageByUserRepository.findById(userId, createAt, messageId).switchIfEmpty(
            Mono.error(MessageNotFoundException("Message with ID $messageId not found for user $userId"))
        ).flatMap {
            messageByUserRepository.markMessageAsRead(
                userId, messageId, createAt
            )
        }
    }
}