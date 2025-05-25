package com.ethyllium.authservice.domain.model

data class TOTPSetupData(
    val secretKey: String,
    val userId: String,
    val qrCodeImageUri: String,
    val issuer: String,
    val username: String
)
