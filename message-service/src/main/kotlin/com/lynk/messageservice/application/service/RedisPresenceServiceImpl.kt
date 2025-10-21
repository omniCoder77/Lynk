package com.lynk.messageservice.application.service

import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class RedisPresenceServiceImpl(
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, String>
) : PresenceService {

    companion object {
        private const val ONLINE_USERS_KEY = "online_users"
    }

    override fun setUserOnline(userId: UUID): Mono<Boolean> {
        return reactiveRedisTemplate.opsForSet().add(ONLINE_USERS_KEY, userId.toString()).map { it > 0 }
    }

    override fun setUserOffline(userId: UUID): Mono<Boolean> {
        return reactiveRedisTemplate.opsForSet().remove(ONLINE_USERS_KEY, userId.toString()).map { it > 0 }
    }

    override fun isUserOnline(userId: UUID): Mono<Boolean> {
        return reactiveRedisTemplate.opsForSet().isMember(ONLINE_USERS_KEY, userId.toString())
    }
}