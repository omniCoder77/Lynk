package com.lynk.authservice.domain.payload.result

sealed interface LoginResult {
    data object TotpRequired : LoginResult
    data object UserNotFound : LoginResult
    data object TotpInvalid : LoginResult
    data object OtpSent : LoginResult
    data class Token(val accessToken: String, val refreshToken: String) : LoginResult
}