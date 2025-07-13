package com.ethyllium.messageservice.infrastructure.adapter.outbound.messaging

data class UserStatus(
    val userId: String,
    val online: Boolean
)