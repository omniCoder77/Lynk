package com.ethyllium.authservice.domain.model

data class OTP(
    val code: String,
    val phone: String,
    val expiryTimeInSeconds: Long
)