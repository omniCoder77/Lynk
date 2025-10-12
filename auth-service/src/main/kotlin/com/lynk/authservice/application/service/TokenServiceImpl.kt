package com.lynk.authservice.application.service

import com.lynk.authservice.domain.port.driven.CacheRepository
import com.lynk.authservice.domain.port.driven.JwtTokenService
import com.lynk.authservice.domain.port.driven.UserRepository
import com.lynk.authservice.domain.port.driver.TokenService
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.temporal.ChronoUnit
import java.util.*

@Component
class TokenServiceImpl(
    private val jwtTokenService: JwtTokenService,
    private val userRepository: UserRepository,
    private val cacheRepository: CacheRepository,
    tokenService: JwtTokenService
) :
    TokenService {

    companion object {
        val EXPIRED_TOKEN_PREFIX = "expired_token:"
    }

    override fun refreshToken(token: String): Mono<String> {
        val claims = jwtTokenService.getClaims(token) ?: throw IllegalArgumentException("Invalid token")
        val phoneNumber = claims.subject
        if (claims.expiration > Date(System.currentTimeMillis())) {
            return Mono.just(token)
        }
        return userRepository.findByPhoneNumber(phoneNumber).map {
            jwtTokenService.generateRefreshToken(phoneNumber)
        }
    }

    override fun logout(token: String): Mono<Boolean> {
        val claims = jwtTokenService.getClaims(token) ?: throw IllegalArgumentException("Invalid token")
        val phoneNumber = claims.subject
        val expiration = claims.expiration
        val leftTimeMillis = expiration.time - System.currentTimeMillis()
        if (leftTimeMillis < 0) return Mono.just(true)// Token is already expired, no need to cache it
        // Cache the token with a prefix to indicate it's expired
        return cacheRepository.put(EXPIRED_TOKEN_PREFIX + phoneNumber, token, leftTimeMillis, ChronoUnit.MILLIS)
    }
}