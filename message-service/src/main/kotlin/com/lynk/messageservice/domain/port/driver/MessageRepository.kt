package com.lynk.messageservice.domain.port.driver

import com.lynk.messageservice.domain.model.Conversation
import com.lynk.messageservice.domain.model.Message
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.ConversationMessageEntity
import reactor.core.publisher.Mono
import java.time.Instant

import reactor.core.publisher.Flux

interface MessageRepository {
    fun get(user1: String, user2: String, start: Instant, end: Instant): Flux<Message>
    fun store(messageContent: String, senderId: String, recipientId: String, timestamp: Instant): Mono<Boolean>
    fun delete(user1: String, user2: String, start: Instant, end: Instant): Mono<Boolean>
}
