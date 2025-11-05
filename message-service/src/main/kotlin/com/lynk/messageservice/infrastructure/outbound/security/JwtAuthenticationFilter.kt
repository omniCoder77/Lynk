package com.lynk.messageservice.infrastructure.outbound.security

import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationFilter(
    private val tokenValidator: JwtTokenValidator
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val authHeader = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange)
        }

        val token = authHeader.substring(7)
        val claims = tokenValidator.getClaimsFromToken(token) ?: return chain.filter(exchange)

        val userId = claims.subject
        val roles = tokenValidator.getRolesFromClaims(claims)
        val phoneNumber = claims["phone_number"]?.toString() ?: throw BadCredentialsException("JWT is invalid: Missing required 'phone_number' claim.")
        val authorities = roles.map { SimpleGrantedAuthority("ROLE_$it") }

        val authentication = LynkAuthenticationToken(userId, null, authorities, phoneNumber)

        return chain.filter(exchange)
            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
    }
}