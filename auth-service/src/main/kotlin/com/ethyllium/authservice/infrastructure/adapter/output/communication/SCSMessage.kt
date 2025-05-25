package com.ethyllium.authservice.infrastructure.adapter.output.communication

import com.ethyllium.authservice.application.dto.UserCreated
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.stereotype.Component

@Component
class SCSMessage(
    private val streamBridge: StreamBridge
) {
    fun sendUserCreated(userId: String) {
        val userCreated = UserCreated(userId)
        streamBridge.send("userCreated-out-0", userCreated)
    }
}