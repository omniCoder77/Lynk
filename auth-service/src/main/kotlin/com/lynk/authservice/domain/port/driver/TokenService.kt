package com.lynk.authservice.domain.port.driver

import reactor.core.publisher.Mono

interface TokenService {
    fun refreshToken(token: String): Mono<String>
    fun logout(token: String): Mono<Boolean>
}