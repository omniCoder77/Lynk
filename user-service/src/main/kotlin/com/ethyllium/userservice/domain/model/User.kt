package com.ethyllium.userservice.domain.model

import java.util.UUID

data class User(
    val userId: UUID,
    val username: String,
    val phoneNumber: String,
    val profile: String,
    val bio: String,
)
