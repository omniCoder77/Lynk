package com.lynk.authservice.infrastructure.inbound.web.filter

import org.springframework.core.annotation.Order
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.time.LocalTime

@Component
@Order(1)
class RateLimitingWebFilter(
    private val redisTemplate: ReactiveRedisTemplate<String, Long>,
    private val script: RedisScript<Boolean>,
) : WebFilter {

    private val maxRequestsPerMinute = 20L
    private val expirySeconds = 59L

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val path = exchange.request.uri.path
        if (!path.startsWith("/api/v1/auth/login") && !path.startsWith("/api/v1/auth/register")) {
            return chain.filter(exchange)
        }

        val remoteAddress = exchange.request.remoteAddress?.hostString ?: "unknown"
        val currentMinute = LocalTime.now().minute
        val key = "rl_${remoteAddress}:${currentMinute}"

        return redisTemplate.execute(script, listOf(key), listOf(maxRequestsPerMinute, expirySeconds)).single(false)
            .flatMap { isLimited ->
                if (isLimited) {
                    exchange.response.statusCode = HttpStatus.TOO_MANY_REQUESTS
                    exchange.response.setComplete()
                } else {
                    chain.filter(exchange)
                }
            }
    }
}