package com.ethyllium.authservice.infrastructure.adapter.output.persistence.repository

import com.ethyllium.authservice.domain.model.User
import com.ethyllium.authservice.domain.repository.UserRepository
import com.ethyllium.authservice.infrastructure.adapter.output.persistence.entity.UserEntity
import com.ethyllium.authservice.infrastructure.adapter.output.persistence.jpa.ReactiveUserRepository
import com.ethyllium.authservice.infrastructure.adapter.output.persistence.mapper.UserMapper
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

@Component
class UserRepositoryImpl(private val jpaUserRepository: ReactiveUserRepository) : UserRepository {

    @Transactional
    override fun save(user: User, password: String): Mono<UserEntity?> {
        return jpaUserRepository.save(UserMapper.toUserEntity(user, password))
    }

    override fun saveSecret(userId: String, secretKey: String, mfaTokenType: String): Mono<Int> {
        return jpaUserRepository.writeSecret(userId, secretKey, mfaTokenType)
    }

    override fun getSecret(userId: String): Mono<String?> {
        return jpaUserRepository.readSecret(userId)
    }

    override fun enableMfa(userId: String): Mono<Boolean> {
        return jpaUserRepository.updateMfaEnabled(userId, true).map { it > 0 }
    }

    override fun disableTOTP(userId: String): Mono<Boolean> {
        return jpaUserRepository.updateMfaEnabled(userId, false).map { it > 0 }
    }

    override fun isTOTPEnabled(userId: String): Mono<Boolean> {
        return jpaUserRepository.mfaState(userId)
    }

    override fun findUserByPhoneNumber(phoneNumber: String): Mono<User> {
        return jpaUserRepository.findUserEntityByPhoneNumber(phoneNumber).map { UserMapper.toUser(it) }
    }
}