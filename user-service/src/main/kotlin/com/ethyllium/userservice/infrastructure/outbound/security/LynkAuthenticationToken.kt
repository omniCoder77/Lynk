package com.ethyllium.userservice.infrastructure.outbound.security

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority

class LynkAuthenticationToken(
    val userId: String,
    val credentials: String? = null,
    authority: List<GrantedAuthority>,
    val phoneNumber: String
) : UsernamePasswordAuthenticationToken(userId, credentials, authority) {
    override fun getCredentials(): Any {
        return this.authorities
    }

    override fun getPrincipal(): Any {
        return this.userId
    }
}