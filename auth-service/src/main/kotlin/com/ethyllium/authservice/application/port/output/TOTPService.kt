package com.ethyllium.authservice.application.port.output

import com.ethyllium.authservice.domain.model.TOTPSetupData
import reactor.core.publisher.Mono

interface TOTPService {
    fun saveTOTPSecret(userId: String, secretKey: String): Mono<Boolean>
    fun getTOTPSecret(userId: String): Mono<String?>
    fun enableTOTP(userId: String): Mono<Boolean>
    fun disableTOTP(userId: String): Mono<Boolean>
    fun isTOTPEnabled(userId: String): Mono<Boolean>
    fun generateTOTPSecret(userId: String, email: String): Mono<TOTPSetupData>
    fun validateTOTPSecret(verificationCode: Int, secretKey: String): Boolean
}