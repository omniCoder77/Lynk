package com.ethyllium.messageservice.infrastructure.input.kafka.dto

data class UserStatus(
    val userId: String,
    val online: Boolean
)