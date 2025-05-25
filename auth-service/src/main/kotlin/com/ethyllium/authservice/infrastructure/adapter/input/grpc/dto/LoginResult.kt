package com.ethyllium.authservice.infrastructure.adapter.input.grpc.dto

sealed interface LoginResult {
    data class Token(val accessToken: String, val refreshToken: String, val tokenExpiration: Long) : LoginResult
    data class MfaRequired(val userId: String): LoginResult
}