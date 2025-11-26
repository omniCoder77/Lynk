package com.lynk.authservice.infrastructure.outbound.persistence.redis

import com.fasterxml.jackson.databind.JsonMappingException
import com.lynk.authservice.TestcontainersConfiguration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.test.StepVerifier
import java.time.temporal.ChronoUnit

@Testcontainers
@SpringBootTest
@Import(TestcontainersConfiguration::class)
class RedisCacheRepositoryTest {

    @Autowired
    private lateinit var redisCacheRepository: RedisCacheRepository

    @Autowired
    private lateinit var reactiveRedisTemplate: ReactiveRedisTemplate<String, String>

    private val testObject = SimpleObject("test", 1)
    private val testKey = "testKey"
    private val testTtl = 10L

    @BeforeEach
    fun cleanup() {
        reactiveRedisTemplate.delete(testKey).block()
    }

    @Test
    fun `put should store value successfully`() {
        StepVerifier.create(
            redisCacheRepository.put(testKey, testObject, testTtl, ChronoUnit.MINUTES)
        ).expectNext(true).verifyComplete()
    }

    @Test
    fun `get should retrieve stored value`() {
        redisCacheRepository.put(testKey, testObject, testTtl, ChronoUnit.MINUTES).block()

        StepVerifier.create(
            redisCacheRepository.get(testKey, SimpleObject::class.java)
        ).expectNext(testObject).verifyComplete()
    }

    @Test
    fun `get should return empty for non-existent key`() {
        StepVerifier.create(
            redisCacheRepository.get("nonExistentKey", SimpleObject::class.java)
        ).verifyComplete()
    }

    @Test
    fun `remove should delete key`() {
        redisCacheRepository.put(testKey, testObject, testTtl, ChronoUnit.MINUTES).block()

        StepVerifier.create(
            redisCacheRepository.remove(testKey)
        ).expectNext(1).verifyComplete()

        StepVerifier.create(
            redisCacheRepository.get(testKey, SimpleObject::class.java)
        ).verifyComplete()
    }

    @Test
    fun `remove should return zero for non-existent key`() {
        StepVerifier.create(
            redisCacheRepository.remove("nonExistentKey")
        ).expectNext(0).verifyComplete()
    }

    @Test
    fun `put should respect TTL`() {
        val shortTtl = 1L
        redisCacheRepository.put(testKey, testObject, shortTtl, ChronoUnit.SECONDS).block()

        Thread.sleep(1500) // Wait for TTL to expire

        StepVerifier.create(
            redisCacheRepository.get(testKey, SimpleObject::class.java)
        ).verifyComplete()
    }

    @Test
    fun `put should handle complex objects`() {
        val complexObject = ComplexObject(
            nested = NestedObject("nested", 2), list = listOf("a", "b", "c"), map = mapOf("key" to "value")
        )

        StepVerifier.create(
            redisCacheRepository.put("complexKey", complexObject, testTtl, ChronoUnit.MINUTES)
        ).expectNext(true).verifyComplete()

        StepVerifier.create(
            redisCacheRepository.get("complexKey", ComplexObject::class.java)
        ).expectNext(complexObject).verifyComplete()
    }

    @Test
    fun `put with empty key should fail`() {
        StepVerifier.create(
            redisCacheRepository.put("", testObject, testTtl, ChronoUnit.MINUTES)
        ).expectError(IllegalArgumentException::class.java).verify()
    }

    @Test
    fun `get with class mismatch should fail`() {
        redisCacheRepository.put(testKey, testObject, testTtl, ChronoUnit.MINUTES).block()

        StepVerifier.create(
            redisCacheRepository.get(testKey, DifferentObject::class.java)
        ).expectError(JsonMappingException::class.java).verify()
    }

    @Test
    fun `should handle special characters in keys`() {
        val specialKey = "special@#$%^&*()Key"

        StepVerifier.create(
            redisCacheRepository.put(specialKey, testObject, testTtl, ChronoUnit.MINUTES)
        ).expectNext(true).verifyComplete()

        StepVerifier.create(
            redisCacheRepository.get(specialKey, SimpleObject::class.java)
        ).expectNext(testObject).verifyComplete()
    }

    @Test
    fun `should handle zero TTL`() {
        StepVerifier.create(
            redisCacheRepository.put(testKey, testObject, 0, ChronoUnit.SECONDS)
        ).expectNext(true).verifyComplete()

        StepVerifier.create(
            redisCacheRepository.get(testKey, SimpleObject::class.java)
        ).expectNextCount(1).verifyComplete()
    }

    data class SimpleObject(val name: String, val id: Int)
    data class DifferentObject(val field: String)
    data class ComplexObject(val nested: NestedObject, val list: List<String>, val map: Map<String, String>)
    data class NestedObject(val name: String, val id: Int)
}