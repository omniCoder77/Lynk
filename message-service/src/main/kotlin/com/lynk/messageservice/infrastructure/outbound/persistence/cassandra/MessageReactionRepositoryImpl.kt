package com.lynk.messageservice.infrastructure.outbound.persistence.cassandra

import com.lynk.messageservice.domain.port.driver.MessageReactionRepository
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.MessageReaction
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.MessageReactionKey
import org.slf4j.LoggerFactory
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

@Repository
class MessageReactionRepositoryImpl(private val reactiveCassandraTemplate: ReactiveCassandraTemplate) :
    MessageReactionRepository {
    private val logger = LoggerFactory.getLogger(this::class.java)
    override fun addReaction(
        roomId: UUID, messageId: UUID, memberId: UUID, emoji: String, reactedAt: Instant
    ): Mono<Boolean> {
        val messageReaction = MessageReaction(
            key = MessageReactionKey(
                roomId = roomId, messageId = messageId, memberId = memberId
            ), reacted_at = reactedAt, emoji = emoji
        )
        return reactiveCassandraTemplate.insert(messageReaction).map { true }.doOnError { logger.error(it.message, it) }
            .onErrorReturn(false)
    }

    override fun deleteReaction(
        roomId: UUID, messageId: UUID, memberId: UUID
    ): Mono<Boolean> {
        return reactiveCassandraTemplate.deleteById(
            MessageReactionKey(
                roomId = roomId,
                messageId = messageId,
                memberId = memberId
            ), MessageReaction::class.java
        )
    }
}