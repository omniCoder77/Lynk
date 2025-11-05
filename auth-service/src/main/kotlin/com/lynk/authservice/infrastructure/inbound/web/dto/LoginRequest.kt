package com.lynk.authservice.infrastructure.inbound.web.dto

data class LoginRequest(
    val phoneNumber: String,
    val totp: String? = null
)
