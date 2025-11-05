package com.lynk.authservice.infrastructure.inbound.web.dto

data class RegisterRequest(
    val phoneNumber: String,
    val firstName: String,
    val lastName: String,
    val mfa: Boolean = false // Multi-Factor Authentication flag
)