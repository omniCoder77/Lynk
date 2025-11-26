package com.lynk.authservice.infrastructure.outbound.persistence.postgres.entity

import com.lynk.authservice.domain.payload.entity.User
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.Instant
import java.util.UUID

@Table("users")
data class UserEntity(
    @Id val userId: UUID = UUID.randomUUID(),
    val totpSecret: String? = null,
    val phoneNumber: String,
    val firstName: String,
    val lastName: String,
    @CreatedDate var createdDate: Instant = Instant.now(),
    @LastModifiedDate var lastModifiedDate: Instant = createdDate,
    val enabled: Boolean = false,
    val isAccountLocked: Boolean = false,
    val role: String = "USER", // comma separated roles
): UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return role.split(",").map { SimpleGrantedAuthority("ROLE_$it") }
    }

    override fun getPassword(): String {
        return ""
    }

    override fun getUsername(): String {
        return userId.toString()
    }

    override fun isEnabled(): Boolean {
        return enabled
    }

    override fun isAccountNonLocked(): Boolean {
        return !isAccountLocked
    }

    fun toUser() = User(
        userId = userId.toString(),
        totpSecret = totpSecret,
        phoneNumber = phoneNumber,
        firstName = firstName,
        lastName = lastName,
        isEnabled = enabled,
        isAccountLocked = isAccountLocked,
        role = role,
    )
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        userId = UUID.fromString(userId),
        totpSecret = totpSecret,
        phoneNumber = phoneNumber,
        firstName = firstName,
        lastName = lastName,
        enabled = isEnabled,
        isAccountLocked = isAccountLocked,
        role = role,
    )
}