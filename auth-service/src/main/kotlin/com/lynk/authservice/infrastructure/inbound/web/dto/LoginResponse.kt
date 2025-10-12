package com.lynk.authservice.infrastructure.inbound.web.dto

sealed interface LoginResponse {
    data class Token(
        val accessToken: String,
        val refreshToken: String
    ) : LoginResponse

    data class OtpSent(val message: String) : LoginResponse

    data class TotpRequired(val message: String) : LoginResponse

    data class TotpInvalid(val message: String) : LoginResponse

    data class UserNotFound(val message: String) : LoginResponse
}
