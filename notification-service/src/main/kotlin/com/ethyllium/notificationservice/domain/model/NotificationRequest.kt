package com.ethyllium.notificationservice.domain.model

data class NotificationRequest(
    val title: String, val body: String, val topic: String, val token: String
)
