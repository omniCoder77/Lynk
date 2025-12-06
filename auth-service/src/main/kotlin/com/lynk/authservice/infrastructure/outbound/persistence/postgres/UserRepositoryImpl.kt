package com.lynk.authservice.infrastructure.outbound.persistence.postgres

import com.lynk.authservice.domain.payload.entity.User
import com.lynk.authservice.domain.port.driven.UserRepository
import com.lynk.authservice.infrastructure.outbound.persistence.postgres.entity.UserEntity
import com.lynk.authservice.infrastructure.outbound.persistence.postgres.entity.toEntity
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.data.relational.core.query.Update
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.*

@Component
class UserRepositoryImpl(private val r2dbcEntityTemplate: R2dbcEntityTemplate) : UserRepository {
    override fun persist(user: User): Mono<UUID> {
        return r2dbcEntityTemplate.insert(user.toEntity()).map { it.userId }.onErrorMap {
            if (it is R2dbcDataIntegrityViolationException) {
                throw DuplicatePhoneNumberException("A user with this phone number already exists.")
            }
            it
        }
    }

    override fun setTotpSecret(it: UUID, totpSecret: String): Mono<Long> {
        val query = Query.query(Criteria.where("user_id").`is`(it))
        val update = Update.update("totp_secret", totpSecret)
        return r2dbcEntityTemplate.update(UserEntity::class.java).matching(query).apply(update)
    }

    override fun delete(it: UUID): Mono<Long> {
        val query = Query.query(Criteria.where("user_id").`is`(it))
        return r2dbcEntityTemplate.delete(query, UserEntity::class.java)
    }

    override fun changeEnableState(phoneNumber: String, state: Boolean): Mono<Long> {
        val query = Query.query(Criteria.where("phone_number").`is`(phoneNumber))
        val update = Update.update("enabled", state)
        return r2dbcEntityTemplate.update(UserEntity::class.java).matching(query).apply(update)
    }

    override fun findByPhoneNumber(phoneNumber: String): Mono<User> {
        val query = Query.query(Criteria.where("phone_number").`is`(phoneNumber))
        return r2dbcEntityTemplate.selectOne(query, UserEntity::class.java)
            .map { it.toUser() }
    }
}