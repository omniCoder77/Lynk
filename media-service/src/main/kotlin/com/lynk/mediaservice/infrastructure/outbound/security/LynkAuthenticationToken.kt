package com.lynk.mediaservice.infrastructure.outbound.security

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority

class LynkAuthenticationToken(
    val principal: String,
    val credentials: String? = null,
    authority: List<GrantedAuthority>,
    val phoneNumber: String
) : UsernamePasswordAuthenticationToken(principal, credentials, authority) {
    override fun getCredentials(): Any {
        return this.authorities
    }

    override fun getPrincipal(): Any {
        return this.principal
    }
}