package com.lynk.authservice.infrastructure.outbound.totp

import com.lynk.authservice.domain.port.driven.TotpValidator
import com.warrenstrange.googleauth.GoogleAuthenticator
import org.springframework.stereotype.Component

@Component
class SecurityCodeValidator: TotpValidator {
    override fun validateCode(secret: String, code: String): Boolean {
        return GoogleAuthenticator().authorize(secret, code.toInt())
    }
}