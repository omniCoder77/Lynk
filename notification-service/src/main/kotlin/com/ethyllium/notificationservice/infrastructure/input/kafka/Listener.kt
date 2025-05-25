package com.ethyllium.notificationservice.infrastructure.input.kafka

import com.ethyllium.notificationservice.domain.model.NotificationRequest
import com.ethyllium.notificationservice.infrastructure.input.kafka.dto.NewMessage
import com.ethyllium.notificationservice.infrastructure.input.kafka.dto.UserCreated
import com.ethyllium.notificationservice.infrastructure.output.adapters.FCMAdapter
import com.ethyllium.notificationservice.infrastructure.output.persistence.entity.Device
import com.ethyllium.notificationservice.infrastructure.output.persistence.respository.DeviceRepository
import com.ethyllium.notificationservice.infrastructure.util.Topic
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.pulsar.reactive.config.annotation.ReactivePulsarListener
import org.springframework.stereotype.Component
import java.util.function.Consumer

@Component
class Listener(
    private val deviceRepository: DeviceRepository, private val fCMAdapter: FCMAdapter
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @ReactivePulsarListener(subscriptionName = "user-created", topics = ["user-created"])
    fun consumeUserCreated(): Consumer<UserCreated> = Consumer { userCreated ->
        logger.info("Received user created from {}", userCreated)
        deviceRepository.save(Device(userCreated.userId, null)).subscribe()
    }

    @ReactivePulsarListener(subscriptionName = "user-message", topics = ["user-message"])
    fun consumeUserMessage(): Consumer<NewMessage> = Consumer { message ->
        deviceRepository.findById(message.receiverId).subscribe { device ->
            val req = NotificationRequest(
                title = message.title,
                body = message.body,
                topic = Topic.NEW_MESSAGE.title,
                token = device.token ?: throw IllegalStateException("Token isn't set for userId: ${message.receiverId}")
            )
            fCMAdapter.sendNotification(req, 84600)
        }

    }
}