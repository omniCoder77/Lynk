package com.ethyllium.userservice.infrastructure.outbound.postgres

import com.ethyllium.userservice.domain.model.User
import com.ethyllium.userservice.domain.port.driver.UserRepository
import com.ethyllium.userservice.infrastructure.outbound.postgres.entity.UserEntity
import com.ethyllium.userservice.infrastructure.outbound.postgres.entity.toEntity
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.data.relational.core.query.Update
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.*

@Component
class UserRepositoryImpl(private val r2dbcEntityTemplate: R2dbcEntityTemplate) : UserRepository {
    override fun insert(user: User): Mono<UUID> {
        return r2dbcEntityTemplate.insert(user.toEntity()).map { it.userId }
    }

    override fun updateUsername(userId: UUID, username: String): Mono<Boolean> {
        val query = Query.query(Criteria.where("user_id").`is`(userId))
        val update = Update.update("username", username)
        return r2dbcEntityTemplate.update(query, update, UserEntity::class.java).map { it > 0 }
    }

    override fun find(userId: UUID): Mono<User> {
        val query = Query.query(Criteria.where("user_id").`is`(userId))
        return r2dbcEntityTemplate.selectOne(query, UserEntity::class.java).map { it.toUser() }
    }

    override fun exist(userId: UUID): Mono<Boolean> {
        val query = Query.query(Criteria.where("user_id").`is`(userId))
        return r2dbcEntityTemplate.exists(query, UserEntity::class.java)
    }

    override fun delete(userId: UUID): Mono<Boolean> {
        val query = Query.query(Criteria.where("user_id").`is`(userId))
        return r2dbcEntityTemplate.delete(query, UserEntity::class.java).map { it > 0 }
    }
}