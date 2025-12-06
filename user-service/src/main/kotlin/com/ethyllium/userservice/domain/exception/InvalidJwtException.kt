package com.ethyllium.userservice.domain.exception

data class InvalidJwtException(override val message: String): RuntimeException(message)