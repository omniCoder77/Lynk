package com.ethyllium.notificationservice.infrastructure.outbound.postgres.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("users")
data class UserEntity(
    @Id val userId: UUID = UUID.randomUUID(),
    val token: String? = null,
)
