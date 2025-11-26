package com.lynk.authservice.application.service

import com.lynk.authservice.domain.payload.result.LoginResult
import com.lynk.authservice.domain.port.driven.*
import com.lynk.authservice.domain.port.driver.LoginService
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.temporal.ChronoUnit

@Service
class LoginServiceImpl(
    private val userRepository: UserRepository,
    private val totpValidator: TotpValidator,
    private val jwtTokenService: JwtTokenService,
    private val smsSender: SmsSender,
    private val cacheRepository: CacheRepository
) : LoginService {

    companion object {
        val otpTTLSeconds = 5 * 60L
    }

    override fun login(phoneNumber: String, totp: String?): Mono<LoginResult> {
        return userRepository.findByPhoneNumber(phoneNumber).flatMap { user ->
                if (user.isEnabled) {
                    if (user.totpSecret != null) {
                        when {
                            totp == null -> Mono.just(LoginResult.TotpRequired)
                            !totpValidator.validateCode(user.totpSecret, totp) -> Mono.just(LoginResult.TotpInvalid)
                            else -> {
                                val accessToken = jwtTokenService.generateAccessToken(phoneNumber)
                                val refreshToken = jwtTokenService.generateRefreshToken(phoneNumber)
                                Mono.just(LoginResult.Token(accessToken, refreshToken))
                            }
                        }
                    } else {
                        val accessToken = jwtTokenService.generateAccessToken(phoneNumber)
                        val refreshToken = jwtTokenService.generateRefreshToken(phoneNumber)
                        Mono.just(LoginResult.Token(accessToken, refreshToken))
                    }
                } else {
                    val otp = OtpGenerator().generateOTP()
                    smsSender.sendSms(phoneNumber, otp)
                    cacheRepository.put(phoneNumber, otp, otpTTLSeconds, ChronoUnit.SECONDS)
                        .then(Mono.just(LoginResult.OtpSent))
                }
            }.switchIfEmpty(Mono.just(LoginResult.UserNotFound))
    }
}