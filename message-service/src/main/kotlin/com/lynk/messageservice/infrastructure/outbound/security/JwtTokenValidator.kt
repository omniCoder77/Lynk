package com.lynk.messageservice.infrastructure.outbound.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class JwtTokenValidator(private val keyProvider: JwtKeyProvider) {

    private lateinit var jwtParser: JwtParser
    private val logger = LoggerFactory.getLogger(this::class.java)

    init {
        jwtParser = Jwts.parser().verifyWith(keyProvider.getKey()).build()
    }

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
        return listOf(claims.get("role", String::class.java))
    }
}