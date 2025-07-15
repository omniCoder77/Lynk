package com.ethyllium.messageservice.domain.port.outbound

import com.ethyllium.messageservice.domain.model.Message
import com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.entity.MessageByUserEntity
import com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.entity.MessageByUserKey
import org.springframework.data.domain.Pageable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface MessageByUserRepository {
    fun findById(userId: String, createdAt: Long, messageId: String): Mono<Message>
    fun insertAll(userMessagesFlux: List<Message>): Flux<MessageByUserEntity>
    fun update(userId: String, columnName: String, value: Any): Mono<Boolean>
    fun insert(userMessageEntity: MessageByUserEntity): Mono<MessageByUserEntity>
    fun deleteById(userId: String, createdAt: Long, messageId: String): Mono<MessageByUserKey?>
    fun getUserMessages(userId: String, conversationId: String?, days: Int, pageable: Pageable): List<Message>
    fun addReaction(userId: String, messageId: String, emojiCode: String, createdAt: Long): Mono<Boolean>
    fun removeReaction(userId: String, messageId: String, emojiCode: String, createdAt: Long): Mono<Boolean>
    fun markMessageAsRead(userId: String, messageId: String, createdAt: Long): Mono<Boolean>
}