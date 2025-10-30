package com.lynk.messageservice.infrastructure.outbound.kafka

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.lynk.messageservice.domain.port.driven.EventPublisher
import com.lynk.messageservice.infrastructure.outbound.kafka.dto.ConversationMessageEvent
import com.lynk.messageservice.infrastructure.outbound.kafka.dto.RoomMessageEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaEventPublisher(private val kafkaTemplate: KafkaTemplate<String, String>) : EventPublisher {

    val objectMapper = jacksonObjectMapper()
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun publish(request: ConversationMessageEvent) {
        val data = objectMapper.writeValueAsString(request)
        kafkaTemplate.send("conversation.message", data).whenComplete { result, ex ->
            if (ex != null) {
                logger.error("Error when publishing message", ex)
                // todo Handle by retrying
            }
        }
    }

    override fun publish(request: RoomMessageEvent) {
        val data = objectMapper.writeValueAsString(request)
        kafkaTemplate.send("room.message", data).whenComplete { result, ex ->
            if (ex != null) {
                logger.error("Error when publishing message", ex)
                // todo Handle by retrying
            }
        }
    }
}