package com.ethyllium.notificationservice.application.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory

@Configuration
class KafkaConsumerConfig(
    @Value("\${spring.kafka.consumer.bootstrap-servers}") private val bootstrapServers: String,
    @Value("\${spring.kafka.properties.security.protocol:PLAINTEXT}") private val securityProtocol: String,
    @Value("\${spring.kafka.properties.ssl.truststore.location:}") private val truststoreLocation: String,
    @Value("\${spring.kafka.properties.ssl.truststore.password:}") private val truststorePassword: String,
    @Value("\${spring.kafka.properties.ssl.keystore.location:}") private val keystoreLocation: String,
    @Value("\${spring.kafka.properties.ssl.keystore.password:}") private val keystorePassword: String,
    @Value("\${spring.kafka.properties.ssl.key.password:}") private val keyPassword: String,
) {

    @Bean
    fun consumerFactory(): ConsumerFactory<String, String> {
        val props = mutableMapOf<String, Any>(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            ConsumerConfig.GROUP_ID_CONFIG to "notification-service",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java
        )

        if (securityProtocol.equals("SSL", ignoreCase = true)) {
            props[org.apache.kafka.common.config.SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG] = ""
            props[org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = securityProtocol
            if (truststoreLocation.isNotBlank()) {
                props[org.apache.kafka.common.config.SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = truststoreLocation
                props[org.apache.kafka.common.config.SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = truststorePassword
            }
            if (keystoreLocation.isNotBlank()) {
                props[org.apache.kafka.common.config.SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG] = keystoreLocation
                props[org.apache.kafka.common.config.SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG] = keystorePassword
                props[org.apache.kafka.common.config.SslConfigs.SSL_KEY_PASSWORD_CONFIG] = keyPassword
            }
        }

        return DefaultKafkaConsumerFactory(props)
    }

    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.consumerFactory = consumerFactory()
        return factory
    }
}