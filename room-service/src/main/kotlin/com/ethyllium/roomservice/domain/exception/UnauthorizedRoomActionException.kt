package com.ethyllium.roomservice.domain.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(code = HttpStatus.UNAUTHORIZED)
class UnauthorizedRoomActionException(override val message: String = "User is not authorized to perform this action on the room"): Exception(message)