package com.ethyllium.notificationservice.infrastructure.input.kafka

import com.ethyllium.notificationservice.infrastructure.input.kafka.dto.UserCreated
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class KafkaConsumer {
    @KafkaListener(topics = ["user-created"])
    fun consumeJson(user: UserCreated) {
        println("Consumed JSON Message: $user")
    }
}