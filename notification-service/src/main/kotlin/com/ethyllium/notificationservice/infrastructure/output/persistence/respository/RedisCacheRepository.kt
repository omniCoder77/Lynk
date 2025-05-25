package com.ethyllium.notificationservice.infrastructure.output.persistence.respository

import com.ethyllium.notificationservice.domain.port.driven.CacheRepository
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Duration

@Component
class RedisCacheRepository(private val reactiveRedisTemplate: ReactiveRedisTemplate<String, Any>) : CacheRepository {
    override fun store(
        key: String, value: Any, timeout: Duration
    ): Mono<Boolean> {
        return reactiveRedisTemplate.opsForValue().set(key, value, timeout)
            .handle { it: Boolean, sink -> sink.next(it) }
    }

    override fun remove(key: String): Mono<Long?> {
        return reactiveRedisTemplate.delete(key)
    }

    override fun <T> read(key: String): Mono<T> {
        return reactiveRedisTemplate.opsForValue().get(key).map { it as T }
    }
}