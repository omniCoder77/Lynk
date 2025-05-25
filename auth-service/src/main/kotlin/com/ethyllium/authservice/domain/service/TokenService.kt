package com.ethyllium.authservice.domain.service

interface TokenService {
    fun validateToken(token: String): String?
    fun generateAccessToken(subject: String, additionalClaims: Map<String, Any> = emptyMap()): String
    fun generateRefreshToken(subject: String, additionalClaims: Map<String, Any> = emptyMap()): String
    fun getSubject(token: String): String?
}