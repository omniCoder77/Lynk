package com.lynk.messageservice.infrastructure.outbound.security

import com.lynk.messageservice.domain.exception.InvalidJwtException
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class JwtTokenValidator(private val keyProvider: JwtKeyProvider) {

    private var jwtParser: JwtParser = Jwts.parser().verifyWith(keyProvider.getKey()).build()
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun getClaimsFromToken(token: String): Claims? {
        return try {
            jwtParser.parseSignedClaims(token).payload
        } catch (e: Exception) {
            logger.warn("Invalid JWT token: ${e.message}")
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun getRolesFromClaims(claims: Claims): List<String> {
        return try {
            listOf(claims.get("role", String::class.java))
        } catch (e: ClassCastException) {
            throw InvalidJwtException(e.message!!)
        } catch (e: NullPointerException) {
            throw InvalidJwtException(e.message!!)
        }
    }
}