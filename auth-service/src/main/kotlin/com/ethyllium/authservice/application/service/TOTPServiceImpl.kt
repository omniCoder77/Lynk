package com.ethyllium.authservice.application.service

import com.ethyllium.authservice.application.port.output.TOTPService
import com.ethyllium.authservice.domain.model.MFAType
import com.ethyllium.authservice.domain.model.TOTPSetupData
import com.ethyllium.authservice.domain.repository.UserRepository
import com.warrenstrange.googleauth.GoogleAuthenticator
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Component
class TOTPServiceImpl(
    private val userRepository: UserRepository,
    @Value("\${totp.issuer:YourCompany}") private val totpIssuer: String,
    @Value("\${totp.period:30}") private val totpPeriod: Int
) : TOTPService {
    override fun saveTOTPSecret(
        userId: String, secretKey: String
    ): Mono<Boolean> {
        return userRepository.saveSecret(userId, secretKey, MFAType.AUTHENTICATOR.name).map { rowsAffected ->
            rowsAffected > 0
        }
    }

    override fun validateTOTPSecret(
        verificationCode: Int,
        secretKey: String
    ): Boolean {
        return GoogleAuthenticator().authorize(secretKey, verificationCode)
    }

    override fun getTOTPSecret(userId: String): Mono<String?> {
        return userRepository.getSecret(userId)
    }

    override fun enableTOTP(userId: String): Mono<Boolean> {
        return userRepository.enableMfa(userId)
    }

    override fun disableTOTP(userId: String): Mono<Boolean> {
        return userRepository.disableTOTP(userId)
    }

    override fun isTOTPEnabled(userId: String): Mono<Boolean> {
        return userRepository.isTOTPEnabled(userId)
    }

    override fun generateTOTPSecret(userId: String, email: String): Mono<TOTPSetupData> {
        val secretKey = GoogleAuthenticator().createCredentials().key

        return getTOTPSecret(userId).defaultIfEmpty("").flatMap { existingSecret ->
            if (existingSecret?.isNotEmpty() ?: false) {
                Mono.just(existingSecret)
            } else {
                saveTOTPSecret(userId, secretKey).thenReturn(secretKey)
            }
        }.publishOn(Schedulers.boundedElastic()).map { finalSecretKey ->
            val encodedIssuer = URLEncoder.encode(totpIssuer, StandardCharsets.UTF_8.toString())
            val encodedAccount = URLEncoder.encode(userId, StandardCharsets.UTF_8.toString())
            val qrCodeUri =
                "otpauth://totp/$encodedIssuer:$encodedAccount?secret=$finalSecretKey&issuer=$encodedIssuer&period=$totpPeriod"

            TOTPSetupData(
                secretKey = finalSecretKey,
                userId = userId,
                qrCodeImageUri = qrCodeUri,
                issuer = totpIssuer,
                username = email
            )
        }
    }

}