package com.lynk.messageservice.infrastructure.inbound.web.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID

data class SendMessageRequest(
    @field:NotBlank(message = "Message content cannot be empty.")
    @field:Size(max = 10000, message = "Message content cannot exceed 5000 characters.")
    val content: String,
    val replyToMessageId: UUID? = null,
    val timestamp: Instant = Instant.now()
)