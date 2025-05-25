package com.ethyllium.authservice.infrastructure.adapter.output.notification

import com.ethyllium.authservice.application.port.output.SmsNotifier
import com.twilio.rest.verify.v2.service.Verification
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Service
class TwilioSmsNotifier(
    @Value("\${twilio.path-service-id}") private val pathServiceId: String
) : SmsNotifier {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun sendOtp(phoneNumber: String): Mono<Void> {
        return Mono.fromCallable {
            val verification = Verification.creator(pathServiceId, phoneNumber, "sms").create()
            logger.info("Sending verification to $phoneNumber", verification.accountSid)
        }.subscribeOn(Schedulers.boundedElastic()).then()
    }
}