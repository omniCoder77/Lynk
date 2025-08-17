package com.lynk.authservice.infrastructure.outbound.persistence.redis

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.lynk.authservice.domain.port.driven.CacheRepository
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.lang.IllegalArgumentException
import java.time.Duration
import java.time.temporal.TemporalUnit

@Component
class RedisCacheRepository(private val reactiveRedisTemplate: ReactiveRedisTemplate<String, String>) : CacheRepository {
    override fun put(
        key: String, value: Any, ttl: Long, unit: TemporalUnit
    ): Mono<Boolean> {
        val value = jacksonObjectMapper().writeValueAsString(value)
        if (key.isEmpty()) return Mono.error(IllegalArgumentException("key is empty"))
        return reactiveRedisTemplate.opsForValue().set(key, value, Duration.of(ttl, unit))
    }

    override fun <T> get(key: String, klass: Class<T>): Mono<T> {
        return reactiveRedisTemplate.opsForValue().get(key).flatMap { value ->
                try {
                    val data = jacksonObjectMapper().readValue(value.toString(), klass)
                    data?.let { Mono.just(it) } ?: Mono.empty()
                } catch (e: Exception) {
                    Mono.error(e)
                }
            }
    }

    override fun remove(key: String): Mono<Long> {
        return reactiveRedisTemplate.delete(key)
    }
}