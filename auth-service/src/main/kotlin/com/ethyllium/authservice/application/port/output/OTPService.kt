package com.ethyllium.authservice.application.port.output

import com.ethyllium.authservice.domain.model.OTP
import reactor.core.publisher.Mono
import java.time.Duration

interface OTPService {
    fun saveOTP(otp: OTP, expiryTime: Duration): Mono<Boolean>
    fun getOTP(phone: String): Mono<String>
    fun deleteOTP(phone: String): Mono<Boolean>
}