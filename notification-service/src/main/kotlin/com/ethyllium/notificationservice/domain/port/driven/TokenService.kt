package com.ethyllium.notificationservice.domain.port.driven

import reactor.core.publisher.Mono
import java.util.UUID

interface TokenService {
    fun saveToken(userId: UUID, token: String): Mono<Boolean>
    fun subscribeTo(userId: UUID, topic: String): Mono<Boolean>
}