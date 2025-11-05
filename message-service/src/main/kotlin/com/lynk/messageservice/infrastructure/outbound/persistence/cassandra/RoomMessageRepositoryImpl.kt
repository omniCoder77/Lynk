package com.lynk.messageservice.infrastructure.outbound.persistence.cassandra

import com.lynk.messageservice.domain.port.driver.RoomMessageRepository
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.RoomMessage
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.RoomMessageKey
import com.lynk.messageservice.infrastructure.util.BucketUtils
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
class RoomMessageRepositoryImpl(private val reactiveCassandraTemplate: ReactiveCassandraTemplate) :
    RoomMessageRepository {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun saveMessage(
        roomId: UUID, content: String, senderId: UUID, messageId: UUID, replyToMessageId: UUID?, timestamp: Instant
    ): Mono<Boolean> {
        val roomMessage = RoomMessage(
            key = RoomMessageKey(roomId = roomId, messageId = messageId, timestamp = timestamp),
            content = content,
            sender_id = senderId,
            reply_to_message_id = replyToMessageId
        )
        return reactiveCassandraTemplate.insert(roomMessage).map { true }.doOnError { logger.error(it.message, it) }
            .onErrorReturn(false)
    }
    override fun getMessagesByRoomId(roomId: UUID, start: Instant, end: Instant): Flux<RoomMessage> {
        val timeBuckets = BucketUtils.generateTimeBuckets(start, end)

        return Flux.fromIterable(timeBuckets).concatMap { bucket ->
            reactiveCassandraTemplate.select(
                Query.query(
                    where("room_id").`is`(roomId),
                    where("time_bucket").`is`(bucket),
                    where("timestamp").gte(start),
                    where("timestamp").lt(end)
                ), RoomMessage::class.java
            )
        }
    }
}