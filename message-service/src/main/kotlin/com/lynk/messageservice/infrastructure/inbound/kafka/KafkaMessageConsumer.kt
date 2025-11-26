package com.lynk.messageservice.infrastructure.inbound.kafka

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.lynk.messageservice.domain.port.driven.ConversationService
import com.lynk.messageservice.domain.port.driven.RoomService
import com.lynk.messageservice.infrastructure.outbound.kafka.dto.ConversationMessageEvent
import com.lynk.messageservice.infrastructure.outbound.kafka.dto.RoomMessageEvent
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Component
class KafkaMessageConsumer(
    private val roomService: RoomService, private val conversationService: ConversationService
) {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val objectMapper = jacksonObjectMapper()

    @KafkaListener(
        topics = ["conversation.message"], concurrency = "20", groupId = "message-service-conversation-group"
    )
    fun consumeConversationMessage(record: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        try {
            val event = objectMapper.readValue(record.value(), ConversationMessageEvent::class.java)
            logger.info(
                "Received ConversationMessageEvent for recipient {} from partition {} with offset {}",
                event.payload.recipientId,
                record.partition(),
                record.offset()
            )

            Mono.fromRunnable<Void> {
                logger.trace("Starting async processing for conversation message from {}", event.senderId)
            }.subscribeOn(Schedulers.boundedElastic()).subscribe({
                acknowledgment.acknowledge()
                logger.trace("Acknowledged conversation message with offset ${record.offset()}")
            }, { e ->
                logger.error(
                    "Error processing conversation message for record at offset ${record.offset()}. Key: ${record.key()}",
                    e
                )
            })

        } catch (e: Exception) {
            logger.error(
                "Error deserializing or initializing processing for conversation message record from topic. Key: ${record.key()}",
                e
            )
            acknowledgment.acknowledge()
        }
    }

    @KafkaListener(
        topics = ["room.message"],
        concurrency = "5",
        containerFactory = "batchKafkaListenerContainerFactory",
        groupId = "message-service-room-group"
    )
    fun consumeRoomMessagesBatch(records: List<ConsumerRecord<String, String>>, acknowledgment: Acknowledgment) {
        if (records.isEmpty()) {
            acknowledgment.acknowledge()
            return
        }

        logger.info(
            "Received batch of ${records.size} RoomMessageEvents for bulk processing. First offset: ${
                records.first().offset()
            }"
        )

        val events = records.mapNotNull { record ->
            try {
                objectMapper.readValue(record.value(), RoomMessageEvent::class.java)
            } catch (e: Exception) {
                logger.error(
                    "Error deserializing record in batch: ${e.message}. Key: ${record.key()}. This record will be skipped.",
                    e
                )
                null
            }
        }

        if (events.isNotEmpty()) {
            Mono.fromRunnable<Void> {
                logger.info("Bulk processing ${events.size} room messages.")
            }.subscribeOn(Schedulers.boundedElastic()).subscribe {
                acknowledgment.acknowledge()
                logger.debug("Acknowledged batch of ${records.size} room messages.")

            }
        } else {
            acknowledgment.acknowledge()
            logger.info("Acknowledged batch of ${records.size} records after deserialization errors.")
        }
    }
}