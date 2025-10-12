package com.lynk.authservice.infrastructure.inbound.web.controller

import com.lynk.authservice.domain.payload.result.OtpVerificationResult
import com.lynk.authservice.domain.payload.result.RegisterResult
import com.lynk.authservice.domain.port.driven.JwtTokenService
import com.lynk.authservice.domain.port.driver.RegisterService
import com.lynk.authservice.infrastructure.inbound.web.dto.RegisterRequest
import com.lynk.authservice.infrastructure.inbound.web.dto.RegisterResponse
import com.lynk.authservice.infrastructure.inbound.web.dto.RegisterResponse.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.*

@RestController
@RequestMapping("/api/v1/auth/register")
class RegisterController(
    private val registerService: RegisterService, private val jwtTokenService: JwtTokenService
) {
    @PostMapping
    fun register(@RequestBody request: RegisterRequest): Mono<ResponseEntity<RegisterResponse>> {
        return registerService.register(
            request.lastName, request.firstName, request.phoneNumber, request.mfa
        ).map {
            when (it) {
                RegisterResult.FirstNameEmpty -> ResponseEntity.badRequest().build()
                RegisterResult.InvalidPhoneNumber -> ResponseEntity.badRequest().build()
                RegisterResult.LastNameEmpty -> ResponseEntity.badRequest().build()
                is RegisterResult.MFAQrCode -> {
                    val base64QrCode = Base64.getEncoder().encodeToString(it.qrCode)
                    ResponseEntity.ok(
                        MFA(
                            qrCode = base64QrCode
                        )
                    )
                }
                RegisterResult.PhoneNumberEmpty -> ResponseEntity.badRequest().build()
                RegisterResult.SmsSendFailed -> ResponseEntity.badRequest().build()
                RegisterResult.Success -> {
                    ResponseEntity.ok(OTP)
                }

            }
        }
    }

    @GetMapping("/{phoneNumber}/{otp}")
    fun verifyOtp(
        @PathVariable otp: String, @PathVariable phoneNumber: String
    ): Mono<ResponseEntity<RegisterResponse>> {
        return registerService.verifyOtp(otp, phoneNumber).map {
            when (it) {
                OtpVerificationResult.ExpiredOTP -> {
                    ResponseEntity.status(408).build()
                }

                OtpVerificationResult.InvalidOtp -> ResponseEntity.badRequest().build()
                OtpVerificationResult.Success -> {
                    val accessToken = jwtTokenService.generateAccessToken(phoneNumber)
                    val refreshToken = jwtTokenService.generateRefreshToken(phoneNumber)
                    ResponseEntity.ok(Token(accessToken, refreshToken))
                }
            }
        }
    }
}