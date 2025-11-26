package com.lynk.messageservice.domain.exception

data class InvalidUUIDException(override val message: String?): Throwable(message)