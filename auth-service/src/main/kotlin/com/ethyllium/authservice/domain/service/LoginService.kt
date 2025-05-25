package com.ethyllium.authservice.domain.service

import com.ethyllium.authservice.infrastructure.adapter.input.grpc.dto.LoginResult
import reactor.core.publisher.Mono

interface LoginService {
    fun login(phoneNumber: String, password: String): Mono<LoginResult>
    fun verify(userId: String, mfaSecret: String) : Mono<LoginResult.Token>
    fun clearAllSession(sessionId: String)
}