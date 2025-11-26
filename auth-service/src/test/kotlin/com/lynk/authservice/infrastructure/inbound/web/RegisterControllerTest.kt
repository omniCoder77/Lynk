package com.lynk.authservice.infrastructure.inbound.web

import com.lynk.authservice.domain.payload.result.OtpVerificationResult
import com.lynk.authservice.domain.payload.result.RegisterResult
import com.lynk.authservice.domain.port.driven.JwtTokenService
import com.lynk.authservice.domain.port.driver.RegisterService
import com.lynk.authservice.infrastructure.inbound.web.controller.RegisterController
import com.lynk.authservice.infrastructure.inbound.web.dto.RegisterRequest
import com.lynk.authservice.infrastructure.inbound.web.dto.RegisterResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.util.*

@WebFluxTest(
    controllers = [RegisterController::class],
    excludeAutoConfiguration = [ReactiveSecurityAutoConfiguration::class]
)
class RegisterControllerTest(
    @Autowired val webTestClient: WebTestClient
) {

    @MockitoBean
    private lateinit var registerService: RegisterService

    @MockitoBean
    private lateinit var jwtTokenService: JwtTokenService

    @Test
    fun `should return 400 when LastNameEmpty`() {
        whenever(registerService.register(any(), any(), any(), any())).thenReturn(Mono.just(RegisterResult.LastNameEmpty))

        val request = RegisterRequest("", "John", "+1234567890", false)

        webTestClient.post().uri("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).bodyValue(request)
            .exchange().expectStatus().isBadRequest
    }

    @Test
    fun `should return 400 when InvalidPhoneNumber`() {
        whenever(
            registerService.register(
                any(),
                any(),
                any(),
                any()
            )
        ).thenReturn(Mono.just(RegisterResult.InvalidPhoneNumber))
        val request = RegisterRequest("Doe", "John", "invalid", false)
        webTestClient.post().uri("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).bodyValue(request)
            .exchange().expectStatus().isBadRequest
    }

    @Test
    fun `should return OTP response when Success`() {
        whenever(registerService.register(any(), any(), any(), any())).thenReturn(Mono.just(RegisterResult.Success))

        val request = RegisterRequest("Doe", "John", "+1234567890", false)

        webTestClient.post().uri("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).bodyValue(request)
            .exchange().expectStatus().isOk.expectBody(RegisterResponse::class.java).value {
                assertThat(it).isEqualTo(RegisterResponse.OTP)
            }
    }

    @Test
    fun `should return QR code when MFAQrCode`() {
        val qrBytes = ByteArray(5) { 1 }
        whenever(registerService.register(any(), any(), any(), any())).thenReturn(
            Mono.just(
                RegisterResult.MFAQrCode(
                    qrBytes
                )
            )
        )

        val request = RegisterRequest("Doe", "John", "+1234567890", true)

        webTestClient.post().uri("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).bodyValue(request)
            .exchange().expectStatus().isOk.expectBody(RegisterResponse.MFA::class.java).value {
                val expectedBase64 = Base64.getEncoder().encodeToString(qrBytes)
                assertThat(it.qrCode).isEqualTo(expectedBase64)
            }
    }

    @Test
    fun `should return 400 when SmsSendFailed`() {
        whenever(registerService.register(any(), any(), any(), any())).thenReturn(Mono.just(RegisterResult.SmsSendFailed))

        val request = RegisterRequest("Doe", "John", "+1234567890", false)

        webTestClient.post().uri("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).bodyValue(request)
            .exchange().expectStatus().isBadRequest
    }

    @Test
    fun `should return 408 when ExpiredOTP`() {
        whenever(
            registerService.verifyOtp(
                eq("123456"),
                eq("+1234567890")
            )
        ).thenReturn(Mono.just(OtpVerificationResult.ExpiredOTP))

        webTestClient.get().uri("/api/v1/auth/register/+1234567890/123456").exchange().expectStatus().isEqualTo(408)
    }

    @Test
    fun `should return 400 when InvalidOtp`() {
        whenever(
            registerService.verifyOtp(
                eq("123456"),
                eq("+1234567890")
            )
        ).thenReturn(Mono.just(OtpVerificationResult.InvalidOtp))

        webTestClient.get().uri("/api/v1/auth/register/+1234567890/123456").exchange().expectStatus().isBadRequest
    }

    @Test
    fun `should return tokens when Success`() {
        whenever(
            registerService.verifyOtp(
                eq("123456"),
                eq("+1234567890")
            )
        ).thenReturn(Mono.just(OtpVerificationResult.Success))
        whenever(jwtTokenService.generateAccessToken("+1234567890")).thenReturn("access123")
        whenever(jwtTokenService.generateRefreshToken("+1234567890")).thenReturn("refresh123")

        webTestClient.get().uri("/api/v1/auth/register/+1234567890/123456").exchange().expectStatus().isOk.expectBody(
            RegisterResponse.Token::class.java
        ).value {
            assertThat(it.accessToken).isEqualTo("access123")
            assertThat(it.refreshToken).isEqualTo("refresh123")
        }
    }
}