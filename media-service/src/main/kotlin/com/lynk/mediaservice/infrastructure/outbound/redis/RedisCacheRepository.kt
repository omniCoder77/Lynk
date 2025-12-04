package com.lynk.mediaservice.infrastructure.outbound.redis

import com.fasterxml.jackson.databind.ObjectMapper
import com.lynk.mediaservice.domain.exception.CacheDeserializationException
import com.lynk.mediaservice.domain.port.driver.CacheRepository
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.temporal.TemporalUnit

@Repository
class RedisCacheRepository(private val reactiveRedisTemplate: ReactiveRedisTemplate<String, String>, private val objectMapper: ObjectMapper):
    CacheRepository {
    override fun put(
        key: String, value: Any, ttl: Long, unit: TemporalUnit
    ): Mono<Boolean> {
        if (key.isEmpty()) return Mono.error(IllegalArgumentException("key is empty"))
        val serializedValue = objectMapper.writeValueAsString(value)
        return reactiveRedisTemplate.opsForValue().set(key, serializedValue, Duration.of(ttl, unit))
    }

    override fun <T> get(key: String, targetClass: Class<T>): Mono<T> {
        return reactiveRedisTemplate.opsForValue().get(key).flatMap { value ->
            try {
                val data = objectMapper.readValue(value, targetClass)
                data?.let { Mono.just(it) } ?: Mono.empty()
            } catch (e: Exception) {
                Mono.error(
                    CacheDeserializationException("Failed to deserialize cache value for key: $key to ${targetClass.simpleName}", e))
            }
        }
    }

    override fun remove(key: String): Mono<Long> {
        return reactiveRedisTemplate.delete(key)
    }
}