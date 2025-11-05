package com.lynk.messageservice.infrastructure.outbound.persistence.cassandra

import com.lynk.messageservice.domain.model.Message
import com.lynk.messageservice.domain.port.driver.MessageRepository
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.ConversationMessageEntity
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.MessageKey
import com.lynk.messageservice.infrastructure.util.BucketUtils
import com.lynk.messageservice.infrastructure.util.BucketUtils.toTimeBucket
import com.lynk.messageservice.infrastructure.util.UUIDUtils
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
class ConversationMessageRepositoryImpl(private val reactiveCassandraTemplate: ReactiveCassandraTemplate) :
    MessageRepository {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun get(user1: String, user2: String, start: Instant, end: Instant): Flux<Message> {
        val conversationId = UUIDUtils.getConversationId(user1, user2)
        val timeBuckets = BucketUtils.generateTimeBuckets(start, end)

        return Flux.fromIterable(timeBuckets).concatMap { bucket ->
            val query = Query.query(
                where("conversation_id").`is`(conversationId),
                where("bucket").`is`(bucket),
                where("message_timestamp").gte(start),
                where("message_timestamp").lt(end)
            )
            reactiveCassandraTemplate.select(query, ConversationMessageEntity::class.java)
        }.doOnError { logger.error("Error fetching messages for conv $conversationId: ${it.message}", it) }
            .map { it.toDomain() }
    }

    override fun store(
        messageContent: String, senderId: String, recipientId: String, timestamp: Instant
    ): Mono<Boolean> {
        val conversationId = UUIDUtils.getConversationId(senderId, recipientId)
        val bucket = timestamp.toTimeBucket()

        val conversationMessageEntity = ConversationMessageEntity(
            key = MessageKey(
                conversationId = conversationId, bucket = bucket, messageTimestamp = timestamp
            ), sender_id = UUID.fromString(senderId), content = messageContent
        )

        return reactiveCassandraTemplate.insert(conversationMessageEntity).map { true }
            .doOnError { logger.error("Error storing message: ${it.message}", it) }.onErrorReturn(false)
    }

    override fun delete(user1: String, user2: String, start: Instant, end: Instant): Mono<Boolean> {
        val conversationId = UUIDUtils.getConversationId(user1, user2)
        val timeBuckets = BucketUtils.generateTimeBuckets(start, end)

        return Flux.fromIterable(timeBuckets).flatMap { bucket ->
            val query = Query.query(
                where("conversation_id").`is`(conversationId),
                where("bucket").`is`(bucket),
                where("message_timestamp").gte(start),
                where("message_timestamp").lt(end)
            )
            reactiveCassandraTemplate.delete(query, ConversationMessageEntity::class.java)
        }.then(Mono.just(true))
            .doOnError { logger.error("Error deleting messages for conv $conversationId: ${it.message}", it) }
            .onErrorReturn(false)
    }
}