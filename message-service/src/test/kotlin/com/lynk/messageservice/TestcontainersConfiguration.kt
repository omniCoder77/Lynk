package com.lynk.messageservice

import com.redis.testcontainers.RedisContainer
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.DependsOn
import org.testcontainers.cassandra.CassandraContainer
import org.testcontainers.cassandra.CassandraQueryWaitStrategy
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.kafka.ConfluentKafkaContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    fun cassandraContainer(): CassandraContainer {
        return CassandraContainer(DockerImageName.parse("cassandra:5.0.6")).withExposedPorts(9042)
            .withInitScript("cassandra-init-data.cql").waitingFor(CassandraQueryWaitStrategy())
    }

    @Bean
    @ServiceConnection(name = "redis")
    @DependsOn("cassandraContainer")
    fun redisContainer(): RedisContainer {
        return RedisContainer(DockerImageName.parse("redis:8.4-rc1-alpine3.22")).withExposedPorts(6379)
    }

    @Bean
    @ServiceConnection(name = "kafka")
    fun kafkaContainer(): ConfluentKafkaContainer {
        return ConfluentKafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:8.1.0")
        ).withEnv(
                mapOf(
                    "KAFKA_NODE_ID" to "1",
                    "KAFKA_PROCESS_ROLES" to "broker,controller",
                    "KAFKA_LISTENERS" to "INTERNAL://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093",
                    "KAFKA_ADVERTISED_LISTENERS" to "INTERNAL://localhost:9092",
                    "KAFKA_LISTENER_SECURITY_PROTOCOL_MAP" to "INTERNAL:PLAINTEXT,CONTROLLER:PLAINTEXT",
                    "KAFKA_CONTROLLER_QUORUM_VOTERS" to "1@localhost:9093",
                    "KAFKA_INTER_BROKER_LISTENER_NAME" to "INTERNAL",
                    "KAFKA_CONTROLLER_LISTENER_NAMES" to "CONTROLLER",
                    "KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR" to "1",
                    "KAFKA_TRANSACTION_STATE_LOG_MIN_ISR" to "1",
                    "KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR" to "1"
                )
            ).withExposedPorts(9092, 9093).waitingFor(Wait.forListeningPort())
    }

}