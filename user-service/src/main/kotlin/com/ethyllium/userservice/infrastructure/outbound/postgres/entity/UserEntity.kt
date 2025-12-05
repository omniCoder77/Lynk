package com.ethyllium.userservice.infrastructure.outbound.postgres.entity

import com.ethyllium.userservice.domain.model.User
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("users")
data class UserEntity(
    @Id val userId: UUID,
    val username: String,
    val phoneNumber: String,
) {
    fun toUser() = User(userId, username, phoneNumber)
}

fun User.toEntity(): UserEntity = UserEntity(userId, username, phoneNumber)