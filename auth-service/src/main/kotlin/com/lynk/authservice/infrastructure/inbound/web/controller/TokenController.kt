package com.lynk.authservice.infrastructure.inbound.web.controller

import com.lynk.authservice.domain.port.driver.TokenService
import com.lynk.authservice.infrastructure.util.toBearerToken
import org.apache.http.HttpHeaders
import org.apache.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/token")
class TokenController(private val tokenService: TokenService) {

    @PostMapping("/refresh")
    fun refreshToken(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String): Mono<ResponseEntity<String>> {
        return tokenService.refreshToken(token.toBearerToken()).map {
            ResponseEntity.status(HttpStatus.SC_OK).body(it)
        }
    }
}