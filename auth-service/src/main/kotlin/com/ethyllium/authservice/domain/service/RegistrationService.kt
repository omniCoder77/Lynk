package com.ethyllium.authservice.domain.service

import com.ethyllium.authservice.domain.model.MFAType
import com.ethyllium.authservice.domain.model.RegistrationResult
import reactor.core.publisher.Mono

interface RegistrationService {
    fun initiateRegistration(name: String, phoneNumber: String): Mono<String>

    fun verifyPhoneNumber(phoneNumber: String, otp: String, sessionId: String): Mono<Boolean>

    fun completeRegistration(
        sessionId: String, password: String, setupMfa: Boolean = false, preferredMfaType: MFAType = MFAType.NONE
    ): Mono<RegistrationResult>
}

