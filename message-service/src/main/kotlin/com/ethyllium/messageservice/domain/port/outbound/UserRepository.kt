package com.ethyllium.messageservice.domain.port.outbound

import reactor.core.publisher.Mono

interface UserRepository {
    fun insertUser(userId: String): Mono<String>
}