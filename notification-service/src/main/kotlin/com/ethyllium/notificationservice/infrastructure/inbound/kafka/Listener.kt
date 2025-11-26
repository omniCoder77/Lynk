package com.ethyllium.notificationservice.infrastructure.inbound.kafka

import com.ethyllium.notificationservice.application.service.NotificationService
import com.ethyllium.notificationservice.domain.port.driver.UserRepository
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import reactor.core.scheduler.Schedulers

@Component
class Listener(
    private val userRepository: UserRepository, private val notificationService: NotificationService
) {

    private val objectMapper = jacksonObjectMapper()
    private val logger = LoggerFactory.getLogger(this::class.java)

    @KafkaListener(topics = ["user.created"], groupId = "notification-service")
    fun userCreated(userCreatedEvent: String) {
        logger.info("Received user created $userCreatedEvent")
        val data = objectMapper.readValue(userCreatedEvent, UserCreatedEvent::class.java)
        userRepository.insert(userId = data.userId).subscribeOn(Schedulers.boundedElastic()).subscribe()
    }

    @KafkaListener(topics = ["conversation.message"], groupId = "notification-service")
    fun sendConversationMessage(message: String) {
        logger.info("Received message $message")
        val data = objectMapper.readValue(message, ConversationMessageEvent::class.java)
        notificationService.sendNotification(data).subscribeOn(Schedulers.boundedElastic()).subscribe()
    }

    @KafkaListener(topics = ["room.message"], groupId = "notification-service")
    fun sendRoomMessage(message: String) {
        logger.info("Received message $message")
        val data = objectMapper.readValue(message, RoomMessageEvent::class.java)
        notificationService.sendNotification(data).subscribeOn(Schedulers.boundedElastic()).subscribe()
    }
}