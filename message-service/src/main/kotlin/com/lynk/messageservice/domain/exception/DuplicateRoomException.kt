package com.lynk.messageservice.domain.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.CONFLICT, code = HttpStatus.CONFLICT, reason = "Can not create room, another room with same name already exists")
class DuplicateRoomException(override val message: String) : RuntimeException(message)