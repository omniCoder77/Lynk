package com.ethyllium.authservice.infrastructure.adapter.output.persistence.repository

import com.ethyllium.authservice.application.port.output.OTPService
import com.ethyllium.authservice.domain.model.OTP
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.Duration

@Repository
class RedisOTPService(
    private val redisOperations: ReactiveRedisOperations<String, Any>
) : OTPService {

    companion object {
        private const val OTP_PREFIX = "otp:"
        private const val TOTP_SECRET_PREFIX = "totp:secret:"
        private const val TOTP_ENABLED_PREFIX = "totp:enabled:"
    }

    override fun saveOTP(otp: OTP, expiryTime: Duration): Mono<Boolean> {
        val key = generateOtpKey(otp.phone)
        return redisOperations.opsForValue().set(key, otp.code, expiryTime)
    }

    override fun getOTP(phone: String): Mono<String> {
        val key = generateOtpKey(phone)
        return redisOperations.opsForValue().get(key).map { it.toString() }
    }

    override fun deleteOTP(phone: String): Mono<Boolean> {
        val key = generateOtpKey(phone)
        return redisOperations.delete(key).map { it > 0 }
    }

    private fun generateOtpKey(phone: String): String {
        return "$OTP_PREFIX$phone"
    }

    private fun generateTotpSecretKey(userId: String): String {
        return "$TOTP_SECRET_PREFIX$userId"
    }

    private fun generateTotpEnabledKey(userId: String): String {
        return "$TOTP_ENABLED_PREFIX$userId"
    }
}