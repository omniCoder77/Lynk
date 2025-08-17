package com.lynk.authservice.infrastructure.outbound.twilio

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertTrue

@SpringBootTest
class TwilioSmsSenderTest {

    @Autowired
    private lateinit var twilioSmsSender: TwilioSmsSender

    @Test
    fun `test send message successfully`() {
        val res = twilioSmsSender.sendSms("+18777804236", "Test message from Twilio")
        assertThat(res).isTrue()
    }

    @Test
    fun `test send message with invalid phone number`() {
        val res = twilioSmsSender.sendSms("invalid-phone-number", "Test message from Twilio")
        assertThat(res).isFalse()
    }
}