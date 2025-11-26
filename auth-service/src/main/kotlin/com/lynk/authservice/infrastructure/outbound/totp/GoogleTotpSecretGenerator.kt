package com.lynk.authservice.infrastructure.outbound.totp

import com.lynk.authservice.domain.port.driven.TotpSecretGenerator
import com.warrenstrange.googleauth.GoogleAuthenticator
import com.warrenstrange.googleauth.GoogleAuthenticatorKey
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class GoogleTotpSecretGenerator(
    @Value("\${issuer}") private val issuer: String
): TotpSecretGenerator {
    override fun generateTotpSecret(phoneNumber: String): Pair<String, String> {
        val key: GoogleAuthenticatorKey = GoogleAuthenticator().createCredentials()
        val secret = key.key
        val otpUri = buildOtpAuthUri(secret, issuer, phoneNumber)
        return Pair(secret, otpUri)
    }

    private fun buildOtpAuthUri(secret: String, issuer: String, account: String): String {
        return "otpauth://totp/${issuer}:${account}?" + "secret=${secret}&" + "issuer=${issuer}&" + "algorithm=SHA1&" + "digits=6&" + "period=30"
    }
}