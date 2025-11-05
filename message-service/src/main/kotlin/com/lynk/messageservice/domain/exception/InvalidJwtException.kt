package com.lynk.messageservice.domain.exception

class InvalidJwtException(override val message: String): RuntimeException(message)