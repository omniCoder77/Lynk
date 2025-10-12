package com.lynk.authservice.application.service

import com.lynk.authservice.domain.payload.entity.User
import com.lynk.authservice.domain.payload.result.OtpVerificationResult
import com.lynk.authservice.domain.payload.result.RegisterResult
import com.lynk.authservice.domain.port.driven.*
import com.lynk.authservice.domain.port.driver.RegisterService
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.temporal.ChronoUnit

@Component
class RegisterServiceImpl(
    private val userRepository: UserRepository,
    private val smsSender: SmsSender,
    private val totpSecretGenerator: TotpSecretGenerator,
    private val qrCodeGenerator: QrCodeGenerator,
    private val cacheRepository: CacheRepository
) : RegisterService {

    companion object {
        private val otpTTLSeconds = 5 * 60L
    }

    override fun register(
        lastName: String, firstName: String, phoneNumber: String, mfa: Boolean
    ): Mono<RegisterResult> {
        if (lastName.isEmpty()) return Mono.just(RegisterResult.LastNameEmpty)
        if (firstName.isEmpty()) return Mono.just(RegisterResult.FirstNameEmpty)
        if (phoneNumber.isEmpty()) return Mono.just(RegisterResult.PhoneNumberEmpty)
        if (!phoneNumber.matches(Regex("^\\+?[0-9]{10,15}$"))) return Mono.just(RegisterResult.InvalidPhoneNumber)

        val user = User(
            lastName = lastName, firstName = firstName, phoneNumber = phoneNumber
        )

        return userRepository.persist(user).flatMap { savedUserId ->
            val otp = OtpGenerator().generateOTP()
            val sendOtpRes = smsSender.sendSms(user.phoneNumber, "Your OTP code is: $otp")
            cacheRepository.put(phoneNumber, otp, otpTTLSeconds, ChronoUnit.SECONDS)
                .subscribeOn(Schedulers.boundedElastic()).subscribe()

            if (sendOtpRes) {
                if (mfa) {
                    val totpSecret = totpSecretGenerator.generateTotpSecret(user.phoneNumber)
                    userRepository.setTotpSecret(savedUserId, totpSecret.first).map {
                        val qr = qrCodeGenerator.generateQrCode(totpSecret.second, 50, 50)
                        RegisterResult.MFAQrCode(qr)
                    }
                } else {
                    Mono.just(RegisterResult.Success)
                }
            } else {
                userRepository.delete(savedUserId).map {
                    RegisterResult.SmsSendFailed
                }
            }
        }
    }

    override fun verifyOtp(
        otp: String, phoneNumber: String
    ): Mono<OtpVerificationResult> {
        return cacheRepository.get(phoneNumber, String::class.java).switchIfEmpty(Mono.just("")).flatMap {
            when (it) {
                "" -> {
                    Mono.just(OtpVerificationResult.ExpiredOTP)
                }

                otp -> {
                    cacheRepository.remove(phoneNumber).then(userRepository.changeEnableState(phoneNumber, true)).map {
                        OtpVerificationResult.Success
                    }
                }

                else -> {
                    Mono.just<OtpVerificationResult>(
                        OtpVerificationResult.InvalidOtp
                    )
                }
            }
        }
    }
}