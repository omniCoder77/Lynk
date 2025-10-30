package com.ethyllium.notificationservice.infrastructure.inbound.web.controller

import com.ethyllium.notificationservice.domain.port.driven.TokenService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.util.*

@RestController
@RequestMapping("/api/v1")
class TokenController(private val tokenService: TokenService) {

    @PostMapping("/token/{token}")
    fun saveToken(authentication: Authentication, @PathVariable token: String): Mono<ResponseEntity<String>> {
        val userId = try {
            UUID.fromString(authentication.name)
        } catch (_: IllegalArgumentException) {
            return Mono.just(ResponseEntity.badRequest().body("Invalid Jwt Token"))
        }
        return tokenService.saveToken(userId, token).flatMap {
            if (it) Mono.just(ResponseEntity.ok("Successfully registered the token"))
            else Mono.just(ResponseEntity.notFound().build())
        }
    }

    @PostMapping("/subscribe/{topic}")
    fun subscribeToken(@PathVariable topic: String, authentication: Authentication): Mono<ResponseEntity<String>> {
        val userId = try {
            UUID.fromString(authentication.name)
        } catch (_: IllegalArgumentException) {
            return Mono.just(ResponseEntity.badRequest().body("Invalid Jwt Token"))
        }

        return tokenService.subscribeTo(userId, topic).flatMap {
            if (it) Mono.just(ResponseEntity.ok().build())
            else Mono.just(ResponseEntity.notFound().build())
        }
    }
}