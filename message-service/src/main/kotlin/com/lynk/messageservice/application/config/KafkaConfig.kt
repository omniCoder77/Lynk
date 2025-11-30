package com.lynk.messageservice.application.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.listener.ContainerProperties

@Configuration
class KafkaConfig {

    @Bean("conversationMessageTopic")
    fun conversationMessageTopic(): NewTopic {
        return NewTopic("conversation.message", 30, 1)
    }

    @Bean("conversationNotificationTopic")
    fun conversationNotificationTopic(): NewTopic {
        return NewTopic("conversation.notification", 30, 1)
    }

    @Bean("roomMessageTopic")
    fun roomMessageTopic(): NewTopic {
        return NewTopic("room.message", 30, 1)
    }

    @Bean("roomNotificationTopic")
    fun roomNotificationTopic(): NewTopic {
        return NewTopic("room.notification", 30, 1)
    }

    @Bean
    fun batchKafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<String, String>
    ): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.consumerFactory = consumerFactory
        factory.isBatchListener = true
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
        return factory
    }
}