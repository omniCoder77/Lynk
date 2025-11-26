package com.lynk.authservice.infrastructure.inbound.web.controller

import com.lynk.authservice.domain.payload.result.LoginResult
import com.lynk.authservice.domain.port.driver.LoginService
import com.lynk.authservice.infrastructure.inbound.web.dto.LoginRequest
import com.lynk.authservice.infrastructure.inbound.web.dto.LoginResponse
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/auth/login")
class LoginController(private val loginService: LoginService) {

    @PostMapping
    fun login(@RequestBody request: LoginRequest): Mono<ResponseEntity<LoginResponse>> {
        return loginService.login(request.phoneNumber, request.totp).map {
            when (it) {
                LoginResult.OtpSent -> {
                    ResponseEntity(
                        LoginResponse.OtpSent("Please enter the OTP sent to ${request.phoneNumber}"),
                        HttpStatusCode.valueOf(403)
                    )
                }

                is LoginResult.Token -> {
                    ResponseEntity(
                        LoginResponse.Token(
                            accessToken = it.accessToken, refreshToken = it.refreshToken
                        ), HttpStatus.OK
                    )
                }

                LoginResult.TotpInvalid -> {
                    ResponseEntity(LoginResponse.TotpInvalid("Invalid totp given"), HttpStatusCode.valueOf(401))
                }

                LoginResult.TotpRequired -> {
                    ResponseEntity(
                        LoginResponse.TotpRequired("TOTP is required for this user"),
                        HttpStatusCode.valueOf(400)
                    )
                }

                LoginResult.UserNotFound -> {
                    ResponseEntity(
                        LoginResponse.UserNotFound("User not found with phone number ${request.phoneNumber}"),
                        HttpStatusCode.valueOf(404)
                    )
                }
            }
        }
    }
}