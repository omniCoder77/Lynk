package com.ethyllium.roomservice.infrastructure.outbound.postgres

import com.ethyllium.roomservice.domain.model.BannedUser
import com.ethyllium.roomservice.domain.port.driven.BannedUserRepository
import com.ethyllium.roomservice.infrastructure.outbound.postgres.entity.BannedUserEntity
import com.ethyllium.roomservice.infrastructure.outbound.postgres.entity.toEntity
import org.slf4j.LoggerFactory
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.data.relational.core.query.Update
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

@Repository
class BannedUserRepositoryImpl(private val r2dbcEntityTemplate: R2dbcEntityTemplate) : BannedUserRepository {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun insert(bannedUser: BannedUser): Mono<Void> {
        return r2dbcEntityTemplate.insert(bannedUser.toEntity()).then()
    }

    override fun update(bannedUntil: Instant?, bannedId: UUID): Mono<Long> {
        val query = Query.query(Criteria.where("ban_id").`is`(bannedId))
        val update = Update.update("banned_until", bannedUntil)
        return r2dbcEntityTemplate.update(
            query, update, BannedUserEntity::class.java
        )
    }

    override fun delete(bannedId: UUID): Mono<Long> {
        val query = Query.query(Criteria.where("banned_id").`is`(bannedId))
        return r2dbcEntityTemplate.delete(query, BannedUserEntity::class.java)
    }

    override fun select(bannedId: UUID): Mono<BannedUser> {
        val query = Query.query(Criteria.where("banned_id").`is`(bannedId))
        return r2dbcEntityTemplate.selectOne(
            query, BannedUserEntity::class.java
        ).map { it.toDomain() }
    }
}