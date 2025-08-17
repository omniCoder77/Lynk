package com.lynk.authservice.domain.port.driver

import com.lynk.authservice.domain.payload.result.LoginResult
import reactor.core.publisher.Mono

interface LoginService {
    fun login(phoneNumber: String, totp: String?): Mono<LoginResult>
}