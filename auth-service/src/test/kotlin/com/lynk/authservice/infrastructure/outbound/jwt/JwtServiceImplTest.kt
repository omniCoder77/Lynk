package com.lynk.authservice.infrastructure.outbound.jwt

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class JwtServiceImplTest {
    val jwtKeyManager: JwtKeyManager = JwtKeyManager(
        keyStoreFilePath = System.getenv("JWT_KEYSTORE_LOCATION"),
        keyStorePasswordStr = System.getenv("JWT_KEYSTORE_PASSWORD"),
        keyAlias = "jwtKey",
        keyPasswordStr = System.getenv("JWT_KEY_PASSWORD")
    )

    private val jwtService = JwtTokenServiceImpl(
        accessTokenExpiration = 3600000,
        refreshTokenExpiration = 86400000,
        jwtKeyManager = jwtKeyManager
    )

    @Test
    fun `access token is generated`() {
        val token = jwtService.generateAccessToken("user123", emptyMap())
        assertNotNull(token, "Token should not be null")
    }

    @Test
    fun `access token contains subject`() {
        val subject = "testUser"
        val token = jwtService.generateAccessToken(subject, emptyMap())
        val claims = jwtService.getClaims(token)

        assertThat(claims?.subject).isEqualTo(subject)
    }

    @Test
    fun `access token contains custom claims`() {
        val claims = mapOf("role" to "admin", "email" to "user@test.com")
        val token = jwtService.generateAccessToken("user", claims)
        val parsedClaims = jwtService.getClaims(token)

        assertThat(parsedClaims?.get("role")).isEqualTo("admin")
        assertThat(parsedClaims?.get("email")).isEqualTo("user@test.com")
    }

    @Test
    fun `refresh token has longer expiration than access token`() {
        val accessToken = jwtService.generateAccessToken("user", emptyMap())
        val refreshToken = jwtService.generateRefreshToken("user", emptyMap())

        val accessExp = jwtService.getClaims(accessToken)?.expiration?.time
        val refreshExp = jwtService.getClaims(refreshToken)?.expiration?.time

        assertThat(refreshExp).isGreaterThan(accessExp)
    }

    @Test
    fun `valid token is validated successfully`() {
        val token = jwtService.generateAccessToken("validUser", emptyMap())
        val subject = jwtService.validateToken(token)

        assertThat(subject).isEqualTo("validUser")
    }

    @Test
    fun `invalid token returns null on validation`() {
        val result = jwtService.validateToken("invalid.token.string")
        assertThat(result).isNull()
    }

    @Test
    fun `getClaims returns null for malformed token`() {
        val claims = jwtService.getClaims("malformed.token")
        assertThat(claims).isNull()
    }

    @Test
    fun `secure token is generated`() {
        val token = jwtService.generateSecureToken()
        assertThat(token).isNotBlank()
    }

    @Test
    fun `secure tokens are unique`() {
        val token1 = jwtService.generateSecureToken()
        val token2 = jwtService.generateSecureToken()

        assertThat(token1).isNotEqualTo(token2)
    }

    @Test
    fun `test token is generated without expiration`() {
        val token = jwtService.generateTestToken("testUser")
        val claims = jwtService.getClaims(token)

        assertThat(claims?.expiration).isNull()
    }
}