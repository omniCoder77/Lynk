package com.lynk.authservice

import com.lynk.authservice.application.service.TokenServiceImpl
import com.lynk.authservice.infrastructure.outbound.jwt.JwtKeyManager
import com.lynk.authservice.infrastructure.outbound.jwt.JwtTokenServiceImpl

fun main() {
    val jwtKeyManager: JwtKeyManager = JwtKeyManager(
        keyStoreFilePath = System.getenv("JWT_KEYSTORE_LOCATION"),
        keyStorePasswordStr = System.getenv("JWT_KEYSTORE_PASSWORD"),
        keyAlias = "jwtKey",
        keyPasswordStr = System.getenv("JWT_KEY_PASSWORD")
    )

    val tokenService = JwtTokenServiceImpl(5000,5000, jwtKeyManager)
    println(tokenService.generateTestToken("9410295564"))
}