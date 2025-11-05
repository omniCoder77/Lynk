package com.ethyllium.notificationservice.domain.port.driver

import com.ethyllium.notificationservice.infrastructure.outbound.postgres.entity.UserEntity
import reactor.core.publisher.Mono
import java.util.UUID

interface UserRepository {
    fun insert(userId: UUID): Mono<Boolean>
    fun find(userId: UUID): Mono<UserEntity>
    fun addToken(userId: UUID, token: String): Mono<Boolean>
}