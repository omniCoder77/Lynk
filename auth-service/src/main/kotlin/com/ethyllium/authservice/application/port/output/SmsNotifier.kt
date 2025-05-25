package com.ethyllium.authservice.application.port.output

import reactor.core.publisher.Mono

interface SmsNotifier {
    fun sendOtp(phoneNumber: String): Mono<Void>
}
