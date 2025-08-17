package com.lynk.authservice.application.security

import com.lynk.authservice.domain.port.driven.JwtTokenService
import com.lynk.authservice.domain.port.driven.UserRepository
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationManager(
    private val jwtTokenService: JwtTokenService,
    private val userRepository: UserRepository
) : ReactiveAuthenticationManager {

    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        val token = authentication.credentials.toString()

        return Mono.justOrEmpty(jwtTokenService.getSubject(token))
            .flatMap { phoneNumber ->
                userRepository.findByPhoneNumber(phoneNumber)
            }
            .map { user ->
                val authorities = user.role.split(",")
                    .map { SimpleGrantedAuthority("ROLE_$it") }
                
                UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    authorities
                )
            }
    }
}