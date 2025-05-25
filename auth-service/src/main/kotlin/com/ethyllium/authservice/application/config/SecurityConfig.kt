package com.ethyllium.authservice.application.config

import com.ethyllium.authservice.infrastructure.adapter.input.oauth.Oauth2SuccessHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain


@Configuration
@EnableWebSecurity
class SecurityConfig {
    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity, oauth2SuccessHandler: Oauth2SuccessHandler): SecurityFilterChain? {
        http.authorizeHttpRequests { authRequest ->
            authRequest.requestMatchers("/auth/**").permitAll().anyRequest().authenticated()
        }.cors { it.disable() }.csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }.oauth2Login {
                it.successHandler(oauth2SuccessHandler)
            }
        return http.build()
    }
}