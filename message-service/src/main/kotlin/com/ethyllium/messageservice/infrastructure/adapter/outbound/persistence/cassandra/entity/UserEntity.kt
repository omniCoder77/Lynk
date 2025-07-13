package com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.entity

import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.Table

@Table("users")
data class UserEntity(
    @PrimaryKey("id") val id: String,
)