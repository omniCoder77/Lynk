package com.lynk.authservice.domain.exception

data class DuplicatePhoneNumberException(override val message: String): RuntimeException(message)
