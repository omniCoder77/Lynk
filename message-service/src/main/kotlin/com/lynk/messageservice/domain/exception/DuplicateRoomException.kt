package com.lynk.messageservice.domain.exception

class DuplicateRoomException(override val message: String) : RuntimeException(message)