package com.lynk.authservice

import com.redis.testcontainers.RedisContainer
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    fun kafkaContainer(): KafkaContainer {
        return KafkaContainer(DockerImageName.parse("apache/kafka-native:latest"))
    }

    @Bean
    @ServiceConnection
    fun postgresContainer(): PostgreSQLContainer {
        return PostgreSQLContainer(DockerImageName.parse("postgres:latest")).apply {
            withDatabaseName("auth_db")
            withUsername("postgres")
            withPassword("postgres")
        }
    }

    @Bean
    @ServiceConnection(name = "redis")
    fun redisContainer(): RedisContainer {
        return RedisContainer(DockerImageName.parse("redis:latest")).withExposedPorts(6379)
    }

}
