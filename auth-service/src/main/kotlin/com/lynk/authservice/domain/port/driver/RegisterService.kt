package com.lynk.authservice.domain.port.driver

import com.lynk.authservice.domain.payload.result.OtpVerificationResult
import com.lynk.authservice.domain.payload.result.RegisterResult
import com.lynk.authservice.infrastructure.inbound.web.dto.RegisterResponse
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono

interface RegisterService {
    fun register(lastName: String, firstName: String, phoneNumber: String, mfa: Boolean): Mono<RegisterResult>
    fun verifyOtp(otp: String, phoneNumber: String): Mono<OtpVerificationResult>
}