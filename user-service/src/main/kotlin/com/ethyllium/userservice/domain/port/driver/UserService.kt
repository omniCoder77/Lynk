package com.ethyllium.userservice.domain.port.driver

import com.ethyllium.userservice.domain.model.User
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface UserService {
    fun find(userId: UUID): Mono<User>
    fun update(userId: UUID, username: String?, bio: String?, profile: String?): Mono<Boolean>
    fun searchByUsername(username: String, size: Int, offset: Int): Flux<User>
    fun getBlockedUsers(userId: UUID): Flux<User>
}