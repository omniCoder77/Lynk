package com.ethyllium.notificationservice.domain.exception

data class TokenNotExistException(override val message: String): RuntimeException(message)
