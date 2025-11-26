package com.lynk.authservice.domain.payload.entity

import java.util.UUID

data class User(
    val userId: String = UUID.randomUUID().toString(),
    val totpSecret: String? = null,
    val phoneNumber: String,
    val firstName: String,
    val lastName: String,
    val isEnabled: Boolean = false,
    val isAccountLocked: Boolean = false,
    val role: String = "USER",
)