package com.ethyllium.messageservice.application.port.outbound

import reactor.core.publisher.Mono

interface MessageInteractionService {
    fun addReaction(userId: String, messageId: String, emoji: String, createAt: Long): Mono<Boolean>
    fun markMessagesRead(userId: String, messageId: String, createAt: Long): Mono<Boolean>
    fun removeReaction(userId: String, messageId: String, emoji: String, createAt: Long): Mono<Boolean>
}