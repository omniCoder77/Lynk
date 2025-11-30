package com.lynk.messageservice.infrastructure.outbound.kafka

import com.lynk.messageservice.domain.port.driven.EventPublisher
import com.lynk.messageservice.infrastructure.outbound.kafka.dto.ConversationMessageEvent
import com.lynk.messageservice.infrastructure.outbound.kafka.dto.RoomMessageEvent
import org.apache.kafka.clients.admin.NewTopic
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.*

@Component
class KafkaEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    @Qualifier("conversationMessageTopic") private val conversationMessageTopic: NewTopic,
    @Qualifier("conversationNotificationTopic") private val conversationNotificationTopic: NewTopic,
    @Qualifier("roomMessageTopic") private val roomMessageTopic: NewTopic,
    @Qualifier("roomNotificationTopic") private val roomNotificationTopic: NewTopic,
    private val redisTemplate: ReactiveRedisTemplate<String, Boolean>
) : EventPublisher {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun publish(request: ConversationMessageEvent) {
        val conversationKey = createConversationKey(
            request.senderId, request.payload.recipientId
        )

        val recipientId = request.payload.recipientId.toString()
        redisTemplate.hasKey("conn:$recipientId").flatMap { isOnline ->
                val topic = if (isOnline) {
                    conversationMessageTopic.name()
                } else {
                    conversationNotificationTopic.name()
                }
                Mono.fromCallable {
                    kafkaTemplate.send(topic, conversationKey, request).whenComplete { result, ex ->
                            if (ex != null) {
                                logger.error("Error publishing to $topic", ex)
                            } else {
                                logger.debug(
                                    "Published to $topic: partition={}, offset={}",
                                    result.recordMetadata.partition(),
                                    result.recordMetadata.offset()
                                )
                            }
                        }
                }
            }
    }

    override fun publish(request: RoomMessageEvent) {
        val roomKey = request.payload.roomId.toString()

        kafkaTemplate.send(
            roomMessageTopic.name(), roomKey, request
        )

        kafkaTemplate.send(
            roomNotificationTopic.name(), roomKey, request
        )
    }

    private fun createConversationKey(user1: UUID, user2: UUID): String {
        return listOf(user1.toString(), user2.toString()).sorted().joinToString(":")
    }
}