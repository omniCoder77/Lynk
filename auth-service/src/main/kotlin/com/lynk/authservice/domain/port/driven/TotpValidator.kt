package com.lynk.authservice.domain.port.driven

interface TotpValidator {
    fun validateCode(secret: String, code: String): Boolean
}