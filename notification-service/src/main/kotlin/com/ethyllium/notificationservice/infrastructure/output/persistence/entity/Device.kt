package com.ethyllium.notificationservice.infrastructure.output.persistence.entity

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table
data class Device(
    @Column val deviceId: String, // This will be same as userId. User ID will be unique to each user and same across all services
    @Column val token: String?,
)
