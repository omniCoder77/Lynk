package com.lynk.mediaservice.domain.exception

data class InvalidJwtException(override val message: String): RuntimeException(message)
