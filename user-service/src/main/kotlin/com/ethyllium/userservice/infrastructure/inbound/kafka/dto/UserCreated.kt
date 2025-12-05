package com.ethyllium.userservice.infrastructure.inbound.kafka.dto

data class UserCreated(
    val userId: String,
    val username: String,
    val phoneNumber: String,
)
