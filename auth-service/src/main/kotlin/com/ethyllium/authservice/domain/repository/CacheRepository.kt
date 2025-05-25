package com.ethyllium.authservice.domain.repository

import reactor.core.publisher.Mono
import java.time.Duration

interface CacheRepository {
    fun store(key: String, value: Any, timeout: Duration): Mono<Boolean>
    fun remove(key: String): Mono<Long?>
    fun <T> read(key: String): Mono<T>
}