package com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra

import com.ethyllium.messageservice.domain.model.Message
import com.ethyllium.messageservice.domain.port.outbound.MessageByUserRepository
import com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.entity.*
import com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.repository.CassandraMessageByUserRepository
import com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.utils.BucketingUtil
import org.springframework.data.cassandra.core.CassandraOperations
import org.springframework.data.cassandra.core.ReactiveCassandraOperations
import org.springframework.data.cassandra.core.query.Update
import org.springframework.data.cassandra.core.query.query
import org.springframework.data.cassandra.core.query.where
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class MessageByUserRepositoryImpl(
    private val cassandraMessageByUserRepository: CassandraMessageByUserRepository,
    private val reactiveCassandraOperations: ReactiveCassandraOperations,
    private val cassandraOperations: CassandraOperations
) : MessageByUserRepository {
    override fun findById(
        userId: String, createdAt: Long, messageId: String
    ): Mono<Message> {
        val bucket = BucketingUtil.calculateBucket(userId, Instant.ofEpochSecond(createdAt))
        return cassandraMessageByUserRepository.findById(
            MessageByUserKey(
                bucket = bucket, senderId = userId, createdAt = Instant.ofEpochSecond(createdAt), messageId = messageId
            )
        ).map {
            it.toDomainMessage()
        }
    }

    override fun insertAll(userMessagesFlux: List<Message>): Flux<MessageByUserEntity> {
        return cassandraMessageByUserRepository.insert(userMessagesFlux.map {
            val bucket = BucketingUtil.calculateBucket(it.senderId.value, it.createdAt)
            it.toMessageByUserEntity(bucket)
        })
    }

    override fun update(
        userId: String, columnName: String, value: Any
    ): Mono<Boolean> {
        val bucket = BucketingUtil.calculateBucket(userId, Instant.now())
        return reactiveCassandraOperations.update(
            query(where("bucket").`is`(bucket)), Update.update(columnName, value), MessageByUserEntity::class.java
        )
    }

    override fun insert(userMessageEntity: MessageByUserEntity): Mono<MessageByUserEntity> {
        return reactiveCassandraOperations.insert(userMessageEntity)
    }

    override fun deleteById(
        userId: String, createdAt: Long, messageId: String
    ): Mono<MessageByUserKey?> {
        val bucket = BucketingUtil.calculateBucket(userId, Instant.ofEpochSecond(createdAt))
        return reactiveCassandraOperations.delete(
            MessageByUserKey(
                bucket, userId, Instant.ofEpochSecond(createdAt), messageId
            )
        )
    }

    override fun getUserMessages(
        userId: String, conversationId: String?, days: Int, pageable: Pageable
    ): List<Message> {
        val now = Instant.now()
        val startTime = now.minus(days.toLong(), ChronoUnit.DAYS)

        val buckets = (0..(days / 7)).map { offset ->
            val bucketInstant = now.minus(offset * 7L, ChronoUnit.DAYS)
            BucketingUtil.calculateBucket(userId, bucketInstant)
        }

        return buckets.flatMap { bucket ->
            val criteria = query(
                where("bucket").`is`(bucket)
            ).and(where("sender_id").`is`(userId)).and(where("created_at").gte(startTime))

            cassandraOperations.select(criteria, MessageByUserEntity::class.java)
        }.filter { msg ->
            conversationId == null || msg.conversation_id == conversationId
        }.sortedByDescending { it.key.createdAt }.map { it.toDomainMessage() }
    }

    override fun addReaction(
        userId: String, messageId: String, emojiCode: String, createdAt: Long
    ): Mono<Boolean> {
        val bucket = BucketingUtil.calculateBucket(userId, Instant.ofEpochSecond(createdAt))
        return reactiveCassandraOperations.update(
            query(where("key.bucket").`is`(bucket)).and(where("key.message_id").`is`(messageId))
                .and(where("key.sender_id").`is`(userId)),
            Update.empty().addTo("reactions.$emojiCode").append(userId),
            MessageByUserEntity::class.java
        )
    }

    override fun removeReaction(
        userId: String, messageId: String, emojiCode: String, createdAt: Long
    ): Mono<Boolean> {
        val bucket = BucketingUtil.calculateBucket(userId, Instant.ofEpochSecond(createdAt))
        return reactiveCassandraOperations.update(
            query(where("key.bucket").`is`(bucket)).and(where("key.message_id").`is`(messageId))
                .and(where("key.sender_id").`is`(userId)),
            Update.empty().removeFrom("reactions.$emojiCode").value(userId),
            MessageByUserEntity::class.java
        )
    }

    override fun markMessageAsRead(userId: String, messageId: String, createdAt: Long): Mono<Boolean> {
        val bucket = BucketingUtil.calculateBucket(userId, Instant.ofEpochSecond(createdAt))
        return reactiveCassandraOperations.update(
            query(where("key.bucket").`is`(bucket)).and(where("key.message_id").`is`(messageId))
                .and(where("key.sender_id").`is`(userId)),
            Update.update("last_message_read_id", messageId),
            ConversationEntity::class.java
        )
    }
}