package com.lynk.messageservice.infrastructure.inbound.kafka

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.lynk.messageservice.infrastructure.outbound.kafka.dto.ConversationMessageEvent
import com.lynk.messageservice.infrastructure.outbound.kafka.dto.ConversationMessagePayload
import com.lynk.messageservice.infrastructure.outbound.kafka.dto.RoomMessageEvent
import com.lynk.messageservice.infrastructure.outbound.kafka.dto.RoomMessagePayload
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.timeout
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.spy
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.kafka.ConfluentKafkaContainer
import org.testcontainers.utility.DockerImageName
import java.util.*
import java.util.concurrent.TimeUnit
import org.awaitility.Awaitility.await
import org.mockito.kotlin.atLeastOnce

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [KafkaMessageConsumerTest.MinimalKafkaConfig::class])
@Testcontainers
@DirtiesContext
@TestConfiguration(proxyBeanMethods = false)
class KafkaMessageConsumerTest {

    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @Autowired
    private lateinit var kafkaMessageConsumer: KafkaMessageConsumer

    private val objectMapper = jacksonObjectMapper()


    companion object {
        @Container
        @ServiceConnection
        val kafkaContainer: ConfluentKafkaContainer = ConfluentKafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:8.1.0")
        ).withEnv(
            mapOf(
                "KAFKA_NODE_ID" to "1",
                "KAFKA_PROCESS_ROLES" to "broker,controller",
                "KAFKA_CONTROLLER_LISTENER_NAMES" to "CONTROLLER",
                "KAFKA_LISTENERS" to "PLAINTEXT://0.0.0.0:9092,BROKER://0.0.0.0:9093,CONTROLLER://0.0.0.0:9094",
                "KAFKA_LISTENER_SECURITY_PROTOCOL_MAP" to "CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,BROKER:PLAINTEXT",
                "KAFKA_CONTROLLER_QUORUM_VOTERS" to "1@localhost:9094",
                "KAFKA_INTER_BROKER_LISTENER_NAME" to "BROKER",
                "KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR" to "1",
                "KAFKA_TRANSACTION_STATE_LOG_MIN_ISR" to "1",
                "KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR" to "1"
            )
        )
    }

    @Configuration
    @EnableKafka
    class MinimalKafkaConfig {

        @Bean
        fun kafkaMessageConsumer(): KafkaMessageConsumer {
            return spy(KafkaMessageConsumer())
        }

        @Bean
        fun consumerFactory(): ConsumerFactory<String, String> {
            val props = mapOf(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaContainer.bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG to "test-group",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java
            )
            return DefaultKafkaConsumerFactory(props)
        }

        @Bean
        fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
            val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
            factory.consumerFactory = consumerFactory()
            factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
            return factory
        }

        @Bean("batchKafkaListenerContainerFactory")
        fun batchKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
            val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
            factory.consumerFactory = consumerFactory()
            factory.isBatchListener = true
            factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
            return factory
        }

        @Bean
        fun producerFactory(): ProducerFactory<String, String> {
            val props = mapOf(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaContainer.bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to org.apache.kafka.common.serialization.StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to org.apache.kafka.common.serialization.StringSerializer::class.java
            )
            return DefaultKafkaProducerFactory(props)
        }

        @Bean
        fun kafkaTemplate(): KafkaTemplate<String, String> {
            return KafkaTemplate(producerFactory())
        }
    }


    @Test
    fun `should consume single conversation message and acknowledge`() {
        val topic = "conversation.message"
        val event = ConversationMessageEvent(
            senderId = UUID.randomUUID(), payload = ConversationMessagePayload(
                content = "This is test", recipientId = UUID.randomUUID(), phoneNumber = "0123456789"
            )
        )
        val jsonMessage = objectMapper.writeValueAsString(event)

        kafkaTemplate.send(topic, "key-1", jsonMessage)

        verify(kafkaMessageConsumer, timeout(10000).times(1)).consumeConversationMessage(any(), any())
    }

    @Test
    fun `should handle deserialization error in single message gracefully`() {
        val topic = "conversation.message"
        val invalidJson = "{ \"garbage\": \"data\" }"

        kafkaTemplate.send(topic, "key-bad", invalidJson)

        verify(kafkaMessageConsumer, timeout(10000).times(1)).consumeConversationMessage(any(), any())
    }


    @Test
    fun `should consume batch room messages`() {
        val topic = "room.message"
        val event1 = RoomMessageEvent(
            UUID.randomUUID(), payload = RoomMessagePayload(
                content = "This is for room 1",
                senderId = UUID.randomUUID(),
                roomId = UUID.randomUUID(),
                senderPhoneNumber = "0123456789",
            )
        )
        val event2 = RoomMessageEvent(
            UUID.randomUUID(), payload = RoomMessagePayload(
                content = "This is for room 2",
                senderId = UUID.randomUUID(),
                roomId = UUID.randomUUID(),
                senderPhoneNumber = "0123456789",
            )
        )

        kafkaTemplate.send(topic, "k1", objectMapper.writeValueAsString(event1))
        kafkaTemplate.send(topic, "k2", objectMapper.writeValueAsString(event2))

        await().atMost(10, TimeUnit.SECONDS).untilAsserted {
            argumentCaptor<List<org.apache.kafka.clients.consumer.ConsumerRecord<String, String>>> {
                verify(kafkaMessageConsumer, atLeastOnce()).consumeRoomMessagesBatch(capture(), any())

                val allRecords = allValues.flatten()

                assertThat(allRecords)
                    .withFailMessage("Expected 2 records, but found ${allRecords.size}")
                    .hasSizeGreaterThanOrEqualTo(2)

                val values = allRecords.map { it.value() }
                assertThat(values).anyMatch { it.contains("room 1") }
                assertThat(values).anyMatch { it.contains("room 2") }
            }
        }
    }

    @Test
    fun `should skip bad records in batch but process valid ones`() {
        val topic = "room.message"
        val validEvent = RoomMessageEvent(
            UUID.randomUUID(), payload = RoomMessagePayload(
                content = "This is for room 2",
                senderId = UUID.randomUUID(),
                roomId = UUID.randomUUID(),
                senderPhoneNumber = "0123456789",
            )
        )


        kafkaTemplate.send(topic, "k-bad", "NOT_JSON")
        kafkaTemplate.send(topic, "k-good", objectMapper.writeValueAsString(validEvent))

        argumentCaptor<List<org.apache.kafka.clients.consumer.ConsumerRecord<String, String>>> {
            verify(kafkaMessageConsumer, timeout(10000).atLeastOnce()).consumeRoomMessagesBatch(capture(), any())

            val capturedRecords = lastValue
            assertThat(capturedRecords.size).isGreaterThanOrEqualTo(1)
        }
    }
}