package com.lynk.authservice.infrastructure.inbound.web.controller

import com.lynk.authservice.domain.port.driver.TokenService
import com.lynk.authservice.infrastructure.util.toBearerToken
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RequestMapping("/api/v1/auth/logout")
@RestController
class LogoutController(private val tokenService: TokenService) {

    @PostMapping
    fun logout(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String): Mono<ResponseEntity<String>> {
        return tokenService.logout(token.toBearerToken()).map {
            if (it) {
                ResponseEntity.ok("Successfully logged out")
            } else {
                ResponseEntity.internalServerError().build()
            }
        }
    }
}