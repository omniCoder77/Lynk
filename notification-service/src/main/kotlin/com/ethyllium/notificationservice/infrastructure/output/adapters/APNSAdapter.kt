package com.ethyllium.notificationservice.infrastructure.output.adapters

import com.ethyllium.notificationservice.domain.model.NotificationRequest
import com.ethyllium.notificationservice.domain.port.driven.PushNotificationPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class APNSAdapter : PushNotificationPort {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun sendNotification(request: NotificationRequest, ttlSeconds: Long): String? {
        // Implementation for Apple Push Notification Service
        logger.info("Sending APNS notification to: ${request.token}")
        // APNS implementation here
        return "apns-message-id"
    }
}