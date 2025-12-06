package com.ethyllium.userservice.infrastructure.inbound.web.dto

data class CreateConversationRequest(
    val userId: String, // other user will be extracted from auth token
)
