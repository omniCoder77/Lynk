package com.lynk.authservice.infrastructure.outbound.jwt

import com.lynk.authservice.domain.port.driven.JwtTokenService
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.*
import javax.crypto.SecretKey

@Service
class JwtTokenServiceImpl(
    @Value("\${jwt.token.access.token.expiry}") private val accessTokenExpiration: Long,
    @Value("\${jwt.token.refresh.token.expiry}") private val refreshTokenExpiration: Long,
    private val jwtKeyManager: JwtKeyManager
) : JwtTokenService {

    private var key: SecretKey
    private var jwtParser: JwtParser

    init {
        runBlocking {
            key = jwtKeyManager.getKey()
            jwtParser = Jwts.parser().verifyWith(key).build()
        }
    }

    override fun generateAccessToken(subject: String, additionalClaims: Map<String, Any>): String {
        val now = Date()
        val expiryDate = Date(now.time + accessTokenExpiration)

        return Jwts.builder().claims(additionalClaims).subject(subject).issuedAt(now).expiration(expiryDate)
            .signWith(key, Jwts.SIG.HS256).compact()
    }

    fun generateTestToken(
        subject: String,
        additionalClaims: Map<String, Any> = emptyMap(),
    ): String {
        val now = Date()
        return Jwts.builder().claims(additionalClaims).subject(subject).issuedAt(now).signWith(key, Jwts.SIG.HS256)
            .compact()
    }

    override fun generateRefreshToken(subject: String, additionalClaims: Map<String, Any>): String {
        val now = Date()
        val expiryDate = Date(now.time + refreshTokenExpiration)

        return Jwts.builder().claims(additionalClaims).subject(subject).issuedAt(now).expiration(expiryDate)
            .signWith(key, Jwts.SIG.HS256).compact()
    }

    override fun validateToken(token: String): String? {
        return try {
            jwtParser.parseSignedClaims(token).payload.subject
        } catch (_: Exception) {
            null
        }
    }

    override fun getClaims(token: String): Claims? {
        return try {
            jwtParser.parseSignedClaims(token).payload
        } catch (_: Exception) {
            null
        }
    }

    override fun getSubject(token: String): String? {
        return getClaims(token)?.subject
    }

    override fun generateSecureToken(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
    }
}