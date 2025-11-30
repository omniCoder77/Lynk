package com.ethyllium.notificationservice.infrastructure.outbound.postgres

import com.ethyllium.notificationservice.domain.port.driver.UserRepository
import com.ethyllium.notificationservice.infrastructure.outbound.postgres.entity.UserEntity
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.data.relational.core.query.Update
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.*
import java.util.function.Function

@Repository
class UserRepositoryImpl(private val r2dbcEntityTemplate: R2dbcEntityTemplate) : UserRepository {
    override fun insert(userId: UUID): Mono<Boolean> {
        val userEntity = UserEntity(userId = userId)
        return r2dbcEntityTemplate.insert<UserEntity?>(userEntity).map { true }
            .onErrorResume(Exception::class.java, Function { _: Exception? -> Mono.just(false) }).defaultIfEmpty(false)
    }

    override fun find(userId: UUID): Mono<UserEntity> {
        return r2dbcEntityTemplate.selectOne(Query.query(Criteria.where("id").`is`(userId)), UserEntity::class.java)
    }

    override fun addToken(userId: UUID, token: String): Mono<Boolean> {
        val query = Query.query(Criteria.where("user_id").`is`(userId))
        val update = Update.update("token", token)
        return r2dbcEntityTemplate.update(query, update, UserEntity::class.java).map { it > 0 }
    }
}