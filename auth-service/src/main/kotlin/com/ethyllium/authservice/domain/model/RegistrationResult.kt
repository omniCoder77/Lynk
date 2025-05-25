package com.ethyllium.authservice.domain.model

data class RegistrationResult(
    val user: User,
    val userId: String,
    val totpSetupData: TOTPSetupData? = null
)