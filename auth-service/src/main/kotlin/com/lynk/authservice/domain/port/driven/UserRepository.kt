package com.lynk.authservice.domain.port.driven

import com.lynk.authservice.domain.payload.entity.User
import reactor.core.publisher.Mono
import java.util.UUID

interface UserRepository {
    fun persist(user: User): Mono<UUID>
    fun setTotpSecret(it: UUID, totpSecret: String): Mono<Long>
    fun delete(it: UUID): Mono<Long>
    fun changeEnableState(phoneNumber: String, state: Boolean): Mono<Long>
    fun findByPhoneNumber(phoneNumber: String): Mono<User>
}