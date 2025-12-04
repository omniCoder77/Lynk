package com.lynk.mediaservice.domain.port.driver

import reactor.core.publisher.Mono
import java.time.temporal.TemporalUnit

interface CacheRepository {
    fun put(key: String, value: Any, ttl: Long, unit: TemporalUnit): Mono<Boolean>
    fun <T> get(key: String, targetClass: Class<T>): Mono<T>
    fun remove(key: String): Mono<Long>
}