package com.ethyllium.authservice.infrastructure.adapter.output.security

import com.ethyllium.authservice.domain.service.TokenService
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtTokenService(
    private val jwtKeyManager: KeyManager,
    @Value("\${token.access.expiration}") private val accessTokenExpiration: Long,
    @Value("\${token.refresh.expiration}") private val refreshTokenExpiration: Long
) : TokenService {

    val key = jwtKeyManager.getKey()
    override fun generateAccessToken(subject: String, additionalClaims: Map<String, Any>): String {
        val now = Date()
        val expiryDate = Date(now.time + accessTokenExpiration)

        return Jwts.builder().claims(additionalClaims).subject(subject).issuedAt(now).expiration(expiryDate)
            .signWith(key, Jwts.SIG.HS256).compact()
    }

    override fun generateRefreshToken(subject: String, additionalClaims: Map<String, Any>): String {
        val now = Date()
        val expiryDate = Date(now.time + refreshTokenExpiration)

        return Jwts.builder().claims(additionalClaims).subject(subject).issuedAt(now).expiration(expiryDate)
            .signWith(key, Jwts.SIG.HS256).compact()
    }

    override fun validateToken(token: String): String? {
        return try {
            val claims = getClaims(token)?.subject
            claims
        } catch (_: Exception) {
            null
        }
    }

    private fun getClaims(token: String): Claims? {
        return try {
            Jwts.parser().verifyWith(jwtKeyManager.getKey()).build().parseSignedClaims(token).payload
        } catch (_: Exception) {
            null
        }
    }

    override fun getSubject(token: String): String? {
        return getClaims(token)?.subject
    }
}