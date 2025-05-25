package com.ethyllium.authservice.domain.service

import com.ethyllium.authservice.application.port.output.TOTPService
import com.ethyllium.authservice.domain.model.MFAType
import com.ethyllium.authservice.domain.model.RegistrationResult
import com.ethyllium.authservice.domain.model.User
import com.ethyllium.authservice.domain.repository.CacheRepository
import com.ethyllium.authservice.domain.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.security.SecureRandom
import java.time.Duration
import java.util.*

@Service
class RegistrationServiceImpl(
    private val otpService: OtpService,
    private val userRepository: UserRepository,
    private val cacheRepository: CacheRepository,
    @Value("\${session.expiry.time}") private val sessionExpiryTime: Long,
    private val tOTPService: TOTPService
) : RegistrationService {

    private val logger = LoggerFactory.getLogger(RegistrationServiceImpl::class.java)
    private val secureRandom = SecureRandom()
    private val SESSION_EXPIRY_DURATION = Duration.ofSeconds(sessionExpiryTime)
    private val REG_SESSION_PREFIX = "registration_session:"

    override fun initiateRegistration(name: String, phoneNumber: String): Mono<String> {
        logger.info("Initiating registration for phone number: $phoneNumber")

        val sessionId = generateSessionId()
        val registrationData = mapOf(
            "name" to name,
            "phoneNumber" to phoneNumber,
            "timestamp" to System.currentTimeMillis(),
            "phoneVerified" to false
        )

        return cacheRepository.store(REG_SESSION_PREFIX + sessionId, registrationData, SESSION_EXPIRY_DURATION)
            .flatMap { stored ->
                if (stored) {
                    otpService.generateAndSendOtp(phoneNumber)
                    return@flatMap Mono.just(sessionId)
                } else {
                    Mono.error(RuntimeException("Failed to create registration session"))
                }
            }
    }

    override fun verifyPhoneNumber(phoneNumber: String, otp: String, sessionId: String): Mono<Boolean> {
        logger.info("Verifying phone number: $phoneNumber with session: $sessionId")
        return cacheRepository.read<Map<String, Comparable<*>>>(REG_SESSION_PREFIX + sessionId).cast(Map::class.java)
            .flatMap { sessionData ->
                val storedPhoneNumber = sessionData["phoneNumber"] as String
                if (storedPhoneNumber == phoneNumber) {
                    return@flatMap if (otpService.validateOtp(
                            phoneNumber = phoneNumber, otpCode = otp, sessionId = sessionId
                        )
                    ) {
                        val updatedData = sessionData.toMutableMap()
                        updatedData["phoneVerified"] = true
                        cacheRepository.store(
                            REG_SESSION_PREFIX + sessionId, updatedData, SESSION_EXPIRY_DURATION
                        ).thenReturn(true)
                    } else Mono.just(false)
                } else {
                    Mono.just(false)
                }
            }.defaultIfEmpty(false)
    }
    override fun completeRegistration(
        sessionId: String, password: String, setupMfa: Boolean, preferredMfaType: MFAType
    ): Mono<RegistrationResult> {
        return cacheRepository.read<Map<String, Comparable<*>>>(REG_SESSION_PREFIX + sessionId)
            .doOnNext { logger.info("Session data: $it") }.flatMap { sessionData ->

                val phoneVerified = sessionData["phoneVerified"] as? Boolean ?: false

                if (!phoneVerified) {
                    return@flatMap Mono.error(RuntimeException("Phone number not verified"))
                }

                val name = sessionData["name"] as String
                val phoneNumber = sessionData["phoneNumber"] as String

                val user = User(
                    name = name, phoneNumber = phoneNumber, mfaType = MFAType.NONE, mfaToken = null, userId = UUID.randomUUID().toString(), password = password)
                userRepository.save(user, password).flatMap { savedUser ->
                    if (savedUser == null) {
                        return@flatMap Mono.error(RuntimeException("Failed to create user"))
                    }

                    cacheRepository.remove(REG_SESSION_PREFIX + sessionId).then(Mono.defer {
                        if (setupMfa && preferredMfaType == MFAType.AUTHENTICATOR) {
                            tOTPService.generateTOTPSecret(savedUser.userId, savedUser.phoneNumber).map { totpSetupData ->
                                RegistrationResult(
                                    user = user, userId = savedUser.userId, totpSetupData = totpSetupData
                                )
                            }
                        } else {
                            Mono.just(RegistrationResult(user = user, userId = savedUser.toString()))
                        }
                    })
                }
            }
    }

    private fun generateSessionId(): String {
        val bytes = ByteArray(16)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}