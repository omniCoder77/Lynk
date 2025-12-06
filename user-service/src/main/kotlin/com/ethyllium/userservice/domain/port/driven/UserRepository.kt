package com.ethyllium.userservice.domain.port.driven

import com.ethyllium.userservice.domain.model.User
import io.lettuce.core.Limit
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface UserRepository {
    fun insert(user: User): Mono<UUID>
    fun update(userId: UUID, username: String? = null, profile: String? = null, bio: String? = null): Mono<Boolean>
    fun delete(userId: UUID): Mono<Boolean>
    fun find(userId: UUID): Mono<User>
    fun exist(userId: UUID): Mono<Boolean>
    fun findByUsername(username: String, limit: Int, offset: Int): Flux<User>
}