package com.ethyllium.notificationservice.infrastructure.input.kafka

import com.ethyllium.notificationservice.infrastructure.input.kafka.dto.UserCreated
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer


@EnableKafka
@Configuration
class KafkaConfiguration {
    @Bean
    fun consumerFactory(): ConsumerFactory<String, String> {
        val config: MutableMap<String, Any> = HashMap()

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092")
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "group_id")
        config.put(
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java
        )
        config.put(
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java
        )

        return DefaultKafkaConsumerFactory(config)
    }


    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory: ConcurrentKafkaListenerContainerFactory<String, String> = ConcurrentKafkaListenerContainerFactory()
        factory.consumerFactory = consumerFactory()
        return factory
    }


    @Bean
    fun userConsumerFactory(): ConsumerFactory<String, UserCreated> {
        val config: MutableMap<String, Any> = HashMap()

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092")
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "group_json")
        config.put(
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java
        )
        config.put(
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer::class.java
        )
        return DefaultKafkaConsumerFactory(
            config, StringDeserializer(), JsonDeserializer(UserCreated::class.java)
        )
    }

    @Bean
    fun userKafkaListenerFactory(): ConcurrentKafkaListenerContainerFactory<String, UserCreated> {
        val factory: ConcurrentKafkaListenerContainerFactory<String, UserCreated> =
            ConcurrentKafkaListenerContainerFactory<String, UserCreated>()
        factory.consumerFactory = userConsumerFactory()
        return factory
    }
}