package com.lynk.messageservice.infrastructure.outbound.persistence.cassandra

import com.lynk.messageservice.domain.model.Conversation
import com.lynk.messageservice.domain.port.driver.ConversationRepository
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.ConversationEntity
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
    ConversationRepository {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun get(userId: String, start: Instant, end: Instant): Flux<Conversation> {
        val query = Query.query(
            where("user_id").`is`(UUID.fromString(userId)),
            where("last_activity_timestamp").gte(start),
            where("last_activity_timestamp").lt(end)
        )
        return reactiveCassandraTemplate.select(query, ConversationEntity::class.java)
            .doOnError { logger.error("Error querying conversations for user $userId: ${it.message}", it) }
            .map { it.toDomain() }
    }

    override fun store(message: String, senderId: UUID, recipientId: UUID): Mono<Boolean> {
        val conversationEntity = ConversationEntity(
            key = ConversationKey(userId = senderId, recipientId = recipientId), lastActivityTimestamp = Instant.now()
        )

        return reactiveCassandraTemplate.insert(conversationEntity).map { true }
            .doOnError { logger.error(it.message, it) }.onErrorReturn(false)
    }

    override fun exists(userId: UUID, recipientId: UUID): Mono<Boolean> {
        return reactiveCassandraTemplate.exists(
            Query.query(
                where("user_id").`is`(userId), where("recipient_id").`is`(recipientId)
            ), ConversationEntity::class.java
        )
    }

    override fun insert(user1: UUID, user2: UUID): Mono<Boolean> {
        val conversationEntity = ConversationEntity(
            ConversationKey(userId = user1, recipientId = user2), lastActivityTimestamp = Instant.now()
        )
        return reactiveCassandraTemplate.insert(conversationEntity).map { true }.onErrorReturn(false)
    }

    override fun delete(userId: String, start: Instant, end: Instant): Mono<Boolean> {
        val query = Query.query(
            where("user_id").`is`(UUID.fromString(userId)),
            where("last_activity_timestamp").gte(start),
            where("last_activity_timestamp").lt(end)
        )

        return reactiveCassandraTemplate.delete(query, ConversationEntity::class.java)
            .doOnError { logger.error("Error deleting conversations for user $userId: ${it.message}", it) }
            .then(Mono.just(true)).onErrorReturn(false)
    }
}