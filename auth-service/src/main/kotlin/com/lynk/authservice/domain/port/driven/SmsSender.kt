package com.lynk.authservice.domain.port.driven

interface SmsSender {
    fun sendSms(
        phoneNumber: String,
        message: String
    ): Boolean
}