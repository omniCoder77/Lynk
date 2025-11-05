package com.ethyllium.notificationservice.infrastructure.inbound.kafka

import com.ethyllium.notificationservice.application.service.NotificationService
import com.ethyllium.notificationservice.domain.port.driver.UserRepository
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import reactor.core.scheduler.Schedulers

@Component
class Listener(
    private val userRepository: UserRepository, private val notificationService: NotificationService
) {

    private val objectMapper = jacksonObjectMapper()

    @KafkaListener(topics = ["user.created"], groupId = "notification-service")
    fun userCreated(userCreatedEvent: String) {
        val data = objectMapper.readValue(userCreatedEvent, UserCreatedEvent::class.java)
        userRepository.insert(userId = data.userId).subscribeOn(Schedulers.boundedElastic()).subscribe()
    }

    @KafkaListener(topics = ["conversation.message"], groupId = "notification-service")
    fun sendConversationMessage(message: String) {
        val data = objectMapper.readValue(message, ConversationMessageEvent::class.java)
        notificationService.sendNotification(data).subscribeOn(Schedulers.boundedElastic()).subscribe()
    }

    @KafkaListener(topics = ["room.message"], groupId = "notification-service")
    fun sendRoomMessage(message: String) {
        val data = objectMapper.readValue(message, RoomMessageEvent::class.java)
        notificationService.sendNotification(data).subscribeOn(Schedulers.boundedElastic()).subscribe()
    }
}