package com.ethyllium.notificationservice.domain.port.driven

import com.ethyllium.notificationservice.domain.model.NotificationRequest

interface PushNotificationPort {
    fun sendNotification(request: NotificationRequest, ttlSeconds: Long): String?
}