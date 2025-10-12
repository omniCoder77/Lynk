package com.lynk.messageservice.infrastructure.outbound.persistence.cassandra

import com.lynk.messageservice.domain.port.driver.ConversationByUserRepository
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.Conversation
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.ConversationKey
import org.slf4j.LoggerFactory
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate
import org.springframework.data.cassandra.core.query.Query
import org.springframework.data.cassandra.core.query.where
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

@Repository
class ConversationRepositoryImpl(private val reactiveCassandraTemplate: ReactiveCassandraTemplate) :
    ConversationByUserRepository {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun get(userId: String, start: Instant, end: Instant): Flux<Conversation> {
        val query = Query.query(
            where("user_id").`is`(UUID.fromString(userId)),
            where("last_activity_timestamp").gte(start),
            where("last_activity_timestamp").lt(end)
        )
        return reactiveCassandraTemplate.select(query, Conversation::class.java)
            .doOnError { logger.error("Error querying conversations for user $userId: ${it.message}", it) }
    }

    override fun store(message: String, senderId: String, recipientId: String): Mono<Boolean> {
        val conversation = Conversation(
            key = ConversationKey(userId = UUID.fromString(senderId), lastActivityTimestamp = Instant.now(), recipientId = recipientId),
            conversationName = "Room",
            lastMessagePreview = message
        )

        return reactiveCassandraTemplate.insert(conversation).map { true }
            .doOnError { logger.error(it.message, it) }.onErrorReturn(false)
    }

    override fun delete(userId: String, start: Instant, end: Instant): Mono<Boolean> {
        val query = Query.query(
            where("user_id").`is`(UUID.fromString(userId)),
            where("last_activity_timestamp").gte(start),
            where("last_activity_timestamp").lt(end)
        )

        return reactiveCassandraTemplate.delete(query, Conversation::class.java)
            .doOnError { logger.error("Error deleting conversations for user $userId: ${it.message}", it) }
            .then(Mono.just(true))
            .onErrorReturn(false)
    }
}