package com.ethyllium.authservice.domain.repository

import com.ethyllium.authservice.domain.model.User
import com.ethyllium.authservice.infrastructure.adapter.output.persistence.entity.UserEntity
import reactor.core.publisher.Mono

interface UserRepository {
    fun saveSecret(userId: String, secretKey: String, mfaTokenType: String): Mono<Int>
    fun getSecret(userId: String): Mono<String?>
    fun enableMfa(userId: String): Mono<Boolean>
    fun disableTOTP(userId: String): Mono<Boolean>
    fun isTOTPEnabled(userId: String): Mono<Boolean>
    fun findUserByPhoneNumber(phoneNumber: String): Mono<User>
    fun save(user: User, password: String): Mono<UserEntity?>
}