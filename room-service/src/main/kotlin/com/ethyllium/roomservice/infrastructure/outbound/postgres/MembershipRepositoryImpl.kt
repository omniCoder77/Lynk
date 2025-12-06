package com.ethyllium.roomservice.infrastructure.outbound.postgres

import com.ethyllium.roomservice.domain.model.Membership
import com.ethyllium.roomservice.domain.model.RoomRole
import com.ethyllium.roomservice.domain.port.driven.MembershipRepository
import com.ethyllium.roomservice.infrastructure.outbound.postgres.entity.MembershipEntity
import com.ethyllium.roomservice.infrastructure.outbound.postgres.entity.toEntity
import org.slf4j.LoggerFactory
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.data.relational.core.query.Update
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.UUID

@Repository
class MembershipRepositoryImpl(private val r2dbcEntityTemplate: R2dbcEntityTemplate) : MembershipRepository {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun insert(membership: Membership): Mono<Boolean> {
        return r2dbcEntityTemplate.insert(membership.toEntity()).map { true }.onErrorResume {
            logger.error("Error inserting membership for user ${membership.userId} in room ${membership.roomId}")
            Mono.just(false)
        }
    }

    override fun update(role: RoomRole, membershipId: UUID): Mono<Boolean> {
        val update = Update.update("role", role)
        val query = Query.query(Criteria.where("membership_id").`is`(membershipId))
        return r2dbcEntityTemplate.update(
            query, update, MembershipEntity::class.java
        ).map { it > 0 }
    }

    override fun delete(membershipId: UUID): Mono<Boolean> {
        val query = Query.query(Criteria.where("membership_id").`is`(membershipId))
        return r2dbcEntityTemplate.delete(query, MembershipEntity::class.java).map { true }.onErrorResume {
            logger.warn("Error deleting room $membershipId")
            Mono.just(false)
        }
    }

    override fun select(membershipId: UUID): Mono<Membership> {
        val query = Query.query(Criteria.where("membership_id").`is`(membershipId))
        return r2dbcEntityTemplate.selectOne(
            query, MembershipEntity::class.java
        ).map { it.toDomain() }
    }
}