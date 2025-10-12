package com.lynk.messageservice.domain.port.driver

import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.Message
import reactor.core.publisher.Mono
import java.time.Instant

import reactor.core.publisher.Flux

interface MessageRepository {
    fun get(user1: String, user2: String, start: Instant, end: Instant): Flux<Message>
    fun store(message: String, senderId: String, recipientId: String, timestamp: Instant): Mono<Boolean>
    fun delete(user1: String, user2: String, start: Instant, end: Instant): Mono<Boolean>
}
