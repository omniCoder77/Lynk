package com.ethyllium.authservice.domain.service

interface OtpService {
    fun generateAndSendOtp(phone: String)
    fun validateOtp(phoneNumber: String, otpCode: String, sessionId: String): Boolean
}