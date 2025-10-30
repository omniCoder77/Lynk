package com.ethyllium.notificationservice.application.service

import com.ethyllium.notificationservice.domain.exception.TokenNotExistException
import com.ethyllium.notificationservice.domain.model.MessageType
import com.ethyllium.notificationservice.domain.port.driver.UserRepository
import com.ethyllium.notificationservice.infrastructure.inbound.kafka.ConversationMessageEvent
import com.ethyllium.notificationservice.infrastructure.inbound.kafka.RoomMessageEvent
import com.ethyllium.notificationservice.infrastructure.outbound.fcm.FCMChatService
import com.ethyllium.notificationservice.infrastructure.outbound.fcm.dto.ConversationChatNotificationRequest
import com.ethyllium.notificationservice.infrastructure.outbound.fcm.dto.RoomChatNotificationRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Service
class NotificationService(
    private val userRepository: UserRepository, private val fCMChatService: FCMChatService
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun sendNotification(event: ConversationMessageEvent): Mono<Void> {
        return userRepository.find(event.payload.roomId).flatMap { user ->
            if (user.token == null) {
                Mono.error(TokenNotExistException("Token not found for ${user.userId}"))
            } else {
                Mono.fromCallable {
                    val request = ConversationChatNotificationRequest(
                        token = user.token,
                        body = event.payload.content,
                        event.payload.recipientId.toString(),
                        event.senderId.toString(),
                        messageId = event.payload.messageId.toString(),
                        messageType = MessageType.TEXT,
                        null
                    )
                    fCMChatService.sendChatNotification(request)
                }.subscribeOn(Schedulers.boundedElastic()).then()

            }
        }.doOnError { e -> logger.error("Notification failed for room ${event.payload.roomId}", e) }
            .onErrorResume { Mono.empty() }.then()
    }

    fun sendNotification(event: RoomMessageEvent): Mono<Void> {
        return Mono.from {
            RoomChatNotificationRequest(
                topic = event.payload.
            )
            fCMChatService.sendRoomNotification()
        }
    }
}