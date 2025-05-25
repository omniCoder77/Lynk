package com.ethyllium.authservice.domain.service

import com.ethyllium.authservice.application.port.output.TOTPService
import com.ethyllium.authservice.domain.exception.InvalidMfaRequestException
import com.ethyllium.authservice.domain.model.MFAType
import com.ethyllium.authservice.domain.model.User
import com.ethyllium.authservice.domain.repository.CacheRepository
import com.ethyllium.authservice.domain.repository.UserRepository
import com.ethyllium.authservice.infrastructure.adapter.input.grpc.dto.LoginResult
import org.apache.http.auth.InvalidCredentialsException
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration

@Service
class LoginServiceImpl(
    @Value("\${session.expiry.time}") private val sessionExpiryTimeSecond: Long,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenService: TokenService,
    @Value("\${token.access.expiration}") private val accessTokenExpiration: Long,
    private val cacheRepository: CacheRepository,
    private val totpService: TOTPService
) : LoginService {

    private val USER_CACHE_PREFIX = "USER:"
    private val CACHE_DURATION get() = Duration.ofSeconds(sessionExpiryTimeSecond)

    override fun login(phoneNumber: String, password: String): Mono<LoginResult> {
        return userRepository.findUserByPhoneNumber(phoneNumber).flatMap { user ->
            if (!passwordEncoder.matches(
                    password, user.password
                )
            ) return@flatMap Mono.error(InvalidCredentialsException())
            if (user.mfaType != MFAType.NONE) {
                val cacheStoreMono = Mono.fromCallable {
                    cacheRepository.store(
                        "$USER_CACHE_PREFIX${user.userId}", user, CACHE_DURATION
                    )
                }.subscribeOn(Schedulers.boundedElastic())
                val loginResultMono = Mono.just(LoginResult.MfaRequired(user.userId))
                return@flatMap Mono.zip(loginResultMono, cacheStoreMono).map { it.t1 }
            } else generateTokens(user.userId).map {
                LoginResult.Token(it.t1, it.t2, accessTokenExpiration)
            }
        }
    }

    override fun verify(userId: String, mfaSecret: String): Mono<LoginResult.Token> {
        return cacheRepository.read<User>("$USER_CACHE_PREFIX$userId").flatMap { user ->
            validateMfa(user, mfaSecret).flatMap { isValid ->
                if (isValid) {
                    generateTokens(user.userId).flatMap {
                        cacheRepository.remove("$USER_CACHE_PREFIX$userId")
                            .thenReturn(LoginResult.Token(it.t1, it.t2, accessTokenExpiration))
                    }
                } else {
                    return@flatMap Mono.error(InvalidMfaRequestException())
                }
            }
        }
    }

    override fun clearAllSession(sessionId: String) {
        cacheRepository.remove(USER_CACHE_PREFIX + sessionId).subscribe()
    }

    private fun validateMfa(user: User, mfaSecret: String): Mono<Boolean> = when (user.mfaType) {
        MFAType.NONE -> Mono.just(true)
        MFAType.AUTHENTICATOR -> Mono.fromCallable {
            mfaSecret.toIntOrNull()?.let { totpService.validateTOTPSecret(it, user.mfaToken!!) } ?: false
        }

        MFAType.SECURITY_CODE -> Mono.just(mfaSecret == user.mfaToken)
    }

    private fun generateTokens(userId: String) = Mono.zip(
        Mono.fromCallable { tokenService.generateAccessToken(userId) },
        Mono.fromCallable { tokenService.generateRefreshToken(userId) })
}