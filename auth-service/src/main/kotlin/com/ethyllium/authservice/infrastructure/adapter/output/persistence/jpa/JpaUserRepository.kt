package com.ethyllium.authservice.infrastructure.adapter.output.persistence.jpa

import com.ethyllium.authservice.domain.model.User
import com.ethyllium.authservice.infrastructure.adapter.output.persistence.entity.UserEntity
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Mono

interface ReactiveUserRepository : R2dbcRepository<UserEntity, String> {
    @Modifying
    @Query(
        """
    UPDATE users 
    SET mfa_token = :secretKey, 
        mfa_type = :mfaTokenType 
    WHERE user_id = :userId
"""
    )
    fun writeSecret(userId: String, secretKey: String, mfaTokenType: String): Mono<Int>

    @Query("SELECT mfa_token FROM users WHERE user_id = :userId")
    fun readSecret(userId: String): Mono<String?>

    @Modifying
    @Query("update users set is_mfa_enabled= :mfaEnabled WHERE user_id = :userId")
    fun updateMfaEnabled(userId: String, mfaEnabled: Boolean): Mono<Int>

    @Query("select is_mfa_enabled from users where user_id = :userId")
    fun mfaState(userId: String): Mono<Boolean>

    fun findUserEntityByPhoneNumber(phoneNumber: String): Mono<UserEntity>
}