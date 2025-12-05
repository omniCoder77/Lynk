package com.ethyllium.userservice.domain.port.driver

import com.ethyllium.userservice.domain.model.User
import reactor.core.publisher.Mono
import java.util.UUID

interface UserRepository {
    fun insert(user: User): Mono<UUID>
    fun updateUsername(userId: UUID, username: String): Mono<Boolean>
    fun delete(userId: UUID): Mono<Boolean>
    fun find(userId: UUID): Mono<User>
}