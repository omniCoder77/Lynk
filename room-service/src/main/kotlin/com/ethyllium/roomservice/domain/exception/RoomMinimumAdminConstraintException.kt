package com.ethyllium.roomservice.domain.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
class RoomMinimumAdminConstraintException(message: String) : RuntimeException(message)