package com.lynk.authservice.application.config

import com.twilio.Twilio
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TwilioConfig(
    @Value("\${twilio.account.sid}") private val twilioSid: String,
    @Value("\${twilio.auth.token}") private val twilioAuthToken: String
) {
    @PostConstruct
    fun init() {
        Twilio.init(twilioSid,twilioAuthToken)
    }
}