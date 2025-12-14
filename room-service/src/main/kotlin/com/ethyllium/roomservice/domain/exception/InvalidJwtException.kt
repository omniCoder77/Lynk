package com.ethyllium.roomservice.domain.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "Access Token is invalid")
class InvalidJwtException(message: String) : Exception(message)