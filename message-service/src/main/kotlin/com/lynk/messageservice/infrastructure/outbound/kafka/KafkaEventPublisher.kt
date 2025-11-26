package com.lynk.messageservice.infrastructure.outbound.kafka

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.lynk.messageservice.domain.port.driven.EventPublisher
import com.lynk.messageservice.infrastructure.outbound.kafka.dto.ConversationMessageEvent
import com.lynk.messageservice.infrastructure.outbound.kafka.dto.RoomMessageEvent
import org.apache.kafka.clients.admin.NewTopic
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import kotlin.math.log

@Component
class KafkaEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    @Qualifier("roomMessage") private val roomMessage: NewTopic,
    @Qualifier("conversationMessage") private val conversationMessage: NewTopic
) : EventPublisher {
    companion object {
        val objectMapper = jacksonObjectMapper()
    }

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun publish(request: ConversationMessageEvent) {
        val data = objectMapper.writeValueAsString(request)
        val key = listOf(request.senderId.toString(), request.payload.recipientId.toString()).sorted().joinToString(":")
        kafkaTemplate.send(conversationMessage.name(), key, data).whenComplete { res, ex ->
            if (ex != null) {
                logger.error("Error when publishing conversation message", ex)
                // todo Handle by retrying
            }
        }
    }

    override fun publish(request: RoomMessageEvent) {
        val data = objectMapper.writeValueAsString(request)
        kafkaTemplate.send(roomMessage.name(), request.payload.roomId.toString(), data).whenComplete { res, ex ->
            if (ex != null) {
                logger.error("Error when publishing message", ex)
                // todo Handle by retrying
            }
        }
    }
}