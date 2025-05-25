package com.ethyllium.authservice.application.service

import com.ethyllium.authservice.domain.service.OtpService
import com.twilio.rest.verify.v2.service.Verification
import com.twilio.rest.verify.v2.service.VerificationCheck
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class OTPServiceImpl(
    @Value("\${twilio.path-service-id}") private val pathServiceId: String
) : OtpService {

    override fun generateAndSendOtp(phone: String) {
        Verification.creator(pathServiceId, phone, "sms").create()
    }

    override fun validateOtp(phoneNumber: String, otpCode: String, sessionId: String): Boolean {
        val verificationCheck = VerificationCheck.creator(pathServiceId).setTo(phoneNumber).setCode(otpCode).create()
        return verificationCheck.valid
    }

}