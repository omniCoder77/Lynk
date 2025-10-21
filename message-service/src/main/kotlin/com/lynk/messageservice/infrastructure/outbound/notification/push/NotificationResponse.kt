package com.lynk.messageservice.infrastructure.outbound.notification.push

data class NotificationResponse(
    private val status: Int = 0, private val message: String? = null
)