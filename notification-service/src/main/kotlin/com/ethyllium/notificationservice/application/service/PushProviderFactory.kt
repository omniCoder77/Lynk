package com.ethyllium.notificationservice.application.service

import com.ethyllium.notificationservice.domain.port.driven.PushNotificationPort
import com.ethyllium.notificationservice.infrastructure.output.adapters.APNSAdapter
import com.ethyllium.notificationservice.infrastructure.output.adapters.FCMAdapter
import com.ethyllium.notificationservice.infrastructure.output.adapters.WNSAdapter
import org.springframework.stereotype.Component

@Component
class PushProviderFactory(
    private val fcmAdapter: FCMAdapter, private val apnsAdapter: APNSAdapter, private val wnsAdapter: WNSAdapter
) {
    fun getProvider(platform: PushPlatform): PushNotificationPort {
        return when (platform) {
            PushPlatform.ANDROID -> fcmAdapter
            PushPlatform.IOS -> apnsAdapter
            PushPlatform.WINDOWS -> wnsAdapter
        }
    }
}