package com.lynk.messageservice.infrastructure.outbound.notification.push

data class NotificationRequest(
    val title: String? = null,
    val body: String? = null,
    val topic: String? = null,
    val token: String? = null
)
