package com.lynk.authservice.domain.port.driven

import kotlin.random.Random

class OtpGenerator {
    fun generateOTP(length: Int = 6): String {
        require(length in 4..8) { "OTP length should be between 4 and 8 digits" }

        val otp = StringBuilder()
        val random = Random

        repeat(length) {
            otp.append(random.nextInt(0, 10))
        }

        return otp.toString()
    }
}