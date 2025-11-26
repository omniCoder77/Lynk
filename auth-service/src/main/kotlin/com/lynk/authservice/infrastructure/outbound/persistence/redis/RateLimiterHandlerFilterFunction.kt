package com.lynk.authservice.infrastructure.outbound.persistence.redis

import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.HandlerFilterFunction
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.net.InetSocketAddress
import java.time.LocalTime
import java.util.*
import java.util.List


@Component
class RateLimiterHandlerFilterFunction(
    private val redisTemplate: ReactiveRedisTemplate<String, Long>,
    private val script: RedisScript<Boolean>,
): HandlerFilterFunction<ServerResponse, ServerResponse> {

    private val maxRequestPerMinute = 20

    override fun filter(
        request: ServerRequest,
        next: HandlerFunction<ServerResponse?>
    ): Mono<ServerResponse?> {
        val currentMinute = LocalTime.now().minute
        val key = java.lang.String.format("rl_%s:%s", requestAddress(request.remoteAddress()), currentMinute)

        return redisTemplate
            .execute(script, listOf(key), listOf(maxRequestPerMinute, 59))
            .single(false)
            .flatMap({ value ->
                if (value)
                    ServerResponse.status(maxRequestPerMinute).build() else
                    next.handle(request)
            })
    }

    private fun requestAddress(maybeAddress: Optional<InetSocketAddress?>): String {
        return if (maybeAddress.isPresent) maybeAddress.get().hostName else ""
    }
}