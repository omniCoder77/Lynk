package com.ethyllium.notificationservice.infrastructure.output.adapters

import com.ethyllium.notificationservice.domain.model.NotificationRequest
import com.ethyllium.notificationservice.domain.port.driven.PushNotificationPort
import com.google.firebase.messaging.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class FCMAdapter(private val fcmInitializer: FCMInitializer) : PushNotificationPort {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun sendNotification(request: NotificationRequest, ttlSeconds: Long): String? {
        val message = getPreconfiguredMessageToToken(request, ttlSeconds)
        return try {
            val response = FirebaseMessaging.getInstance().sendAsync(message).get()
            logger.info("Sent message to token. Device token: ${request.token}, response: $response")
            response
        } catch (e: Exception) {
            logger.error("Failed to send FCM notification", e)
            null
        }
    }

    private fun getAndroidConfig(topic: String?, ttlSeconds: Long): AndroidConfig {
        return AndroidConfig.builder().setTtl(Duration.ofSeconds(ttlSeconds).toMillis()).setCollapseKey(topic)
            .setPriority(AndroidConfig.Priority.HIGH)
            .setNotification(AndroidNotification.builder().setTag(topic).build()).build()
    }

    private fun getPreconfiguredMessageToToken(request: NotificationRequest, ttlSeconds: Long): Message {
        val androidConfig = getAndroidConfig(request.topic, ttlSeconds)
        val notification = Notification.builder().setTitle(request.title).setBody(request.body).build()

        return Message.builder().setAndroidConfig(androidConfig).setNotification(notification).setToken(request.token)
            .build()
    }
}