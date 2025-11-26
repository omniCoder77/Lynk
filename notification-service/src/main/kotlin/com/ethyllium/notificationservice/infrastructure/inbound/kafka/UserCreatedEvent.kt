package com.ethyllium.notificationservice.infrastructure.inbound.kafka

import java.util.UUID

data class UserCreatedEvent(
    val userId: UUID,
)
