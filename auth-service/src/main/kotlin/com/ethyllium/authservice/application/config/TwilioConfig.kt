package com.ethyllium.authservice.application.config

import com.twilio.Twilio
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class TwilioConfig(
    @Value("\${twilio.account.sid}") private val accountSid: String,
    @Value("\${twilio.auth.token}") private val authToken: String
) {
    @PostConstruct
    fun init() {
        Twilio.init(accountSid,authToken)
    }
}