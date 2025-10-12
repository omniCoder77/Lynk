package com.lynk.authservice.application.config

import com.lynk.authservice.application.security.JwtAuthenticationManager
import com.lynk.authservice.application.security.JwtSecurityContextRepository
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.stereotype.Component

@Component
@EnableWebFluxSecurity
class SecurityConfig(
    private val authenticationManager: JwtAuthenticationManager,
    private val securityContextRepository: JwtSecurityContextRepository
) {

    companion object {
        private val SWAGGER_WHITELIST = arrayOf(
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/api-docs",
            "/api-docs/**",
            "/api-docs.yaml",
            "/webjars/swagger-ui/**"
        )
    }

    @Bean
    fun securityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http.cors { it.disable() }.csrf { it.disable() }.formLogin { it.disable() }.httpBasic { it.disable() }
            .authenticationManager(authenticationManager).securityContextRepository(securityContextRepository)
            .authorizeExchange { authorize ->
                authorize.pathMatchers("/api/v1/auth/**", "/swagger-ui").permitAll().pathMatchers(*SWAGGER_WHITELIST)
                    .permitAll().anyExchange().authenticated()
            }.build()
    }
}