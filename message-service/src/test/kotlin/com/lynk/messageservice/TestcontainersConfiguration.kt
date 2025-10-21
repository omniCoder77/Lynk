package com.lynk.messageservice

import com.redis.testcontainers.RedisContainer
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.DependsOn
import org.testcontainers.cassandra.CassandraContainer
import org.testcontainers.cassandra.wait.CassandraQueryWaitStrategy
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    fun cassandraContainer(): CassandraContainer {
        return CassandraContainer(DockerImageName.parse("cassandra:latest"))
            .withExposedPorts(9042)
            .withInitScript("cassandra-init-data.cql")
            .waitingFor(CassandraQueryWaitStrategy())
    }
    @Bean
    @ServiceConnection(name = "redis")
    @DependsOn("cassandraContainer")
    fun redisContainer(): RedisContainer {
        return RedisContainer(DockerImageName.parse("redis:latest")).withExposedPorts(6379)
    }
}