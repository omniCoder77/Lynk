package com.lynk.authservice.domain.port.driven

interface TotpSecretGenerator {
    fun generateTotpSecret(phoneNumber: String): Pair<String, String>
}