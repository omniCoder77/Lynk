package com.lynk.authservice.infrastructure.outbound.twilio

import com.lynk.authservice.domain.port.driven.SmsSender
import com.twilio.rest.api.v2010.account.Message
import com.twilio.type.PhoneNumber
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class TwilioSmsSender(
    @Value("\${twilio.phone.number}") private val senderPhoneNumber: String
) : SmsSender {
    override fun sendSms(phoneNumber: String, message: String): Boolean {
        return try {
            Message.creator(PhoneNumber(phoneNumber), PhoneNumber(senderPhoneNumber), message).create()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }

    }
}