package com.ethyllium.userservice.infrastructure.inbound.kafka

import com.ethyllium.userservice.domain.exception.UserCreationException
import com.ethyllium.userservice.domain.model.User
import com.ethyllium.userservice.domain.port.driver.UserRepository
import com.ethyllium.userservice.infrastructure.inbound.kafka.dto.UserCreated
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.util.*

@Component
class KafkaListener(
    private val objectMapper: ObjectMapper, private val userRepository: UserRepository
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @KafkaListener(topics = ["user.created"], groupId = "user-group-id")
    fun userCreated(record: ConsumerRecord<String, String>, acknowledgment: Acknowledgment) {
        val userCreatedEvent = try {
            objectMapper.readValue(record.value(), UserCreated::class.java)
        } catch (e: JsonProcessingException) {
            logger.error("Failed to parse UserCreated event from Kafka message", e)
            acknowledgment.acknowledge()
            return
        }
        val userId = try {
            UUID.fromString(userCreatedEvent.userId)
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid UUID format for userId: ${userCreatedEvent.userId}", e)
            acknowledgment.acknowledge()
            return
        }
        val user = User(
            userId = userId, username = userCreatedEvent.username, phoneNumber = userCreatedEvent.phoneNumber
        )
        userRepository.insert(user).blockOptional().orElseThrow {
            // TODO send message to DLQ
            logger.error("Failed to insert user: ${user.userId}")
            UserCreationException("User insertion failed")
        }.also { _ ->
            acknowledgment.acknowledge()
        }
    }
}