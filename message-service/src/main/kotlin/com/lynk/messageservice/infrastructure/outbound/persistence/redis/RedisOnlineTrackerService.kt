package com.lynk.messageservice.infrastructure.outbound.persistence.redis

import com.lynk.messageservice.domain.port.driven.OnlineTrackerService
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Duration

@Component
class RedisOnlineTrackerService(
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, String>
) : OnlineTrackerService {

    private val key = "online-users"

    override fun setUserOnline(userId: String): Mono<Void> {
        return reactiveRedisTemplate.opsForSet().add(key, userId).then(
                reactiveRedisTemplate.expire(key, Duration.ofHours(1)).then()
            )
    }

    override fun setUserOffline(userId: String): Mono<Void> {
        return reactiveRedisTemplate.opsForSet().remove(key, userId).then()
    }

    override fun isOnline(userId: String): Mono<Boolean> {
        return reactiveRedisTemplate.opsForSet().isMember(key, userId)
    }
}
