package com.ethyllium.roomservice.infrastructure.outbound.postgres

import com.ethyllium.roomservice.domain.model.Membership
import com.ethyllium.roomservice.domain.model.RoomRole
import com.ethyllium.roomservice.domain.port.driven.MembershipRepository
import com.ethyllium.roomservice.infrastructure.outbound.postgres.entity.MembershipEntity
import com.ethyllium.roomservice.infrastructure.outbound.postgres.entity.toEntity
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.data.relational.core.query.Update
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Repository
class MembershipRepositoryImpl(
    private val r2dbcEntityTemplate: R2dbcEntityTemplate
) : MembershipRepository {

    override fun insert(membership: Membership): Mono<Void> =
        r2dbcEntityTemplate.insert(membership.toEntity()).then()

    override fun update(role: RoomRole, membershipId: UUID): Mono<Long> {
        val update = Update.update("role", role.name)
        val query = Query.query(Criteria.where("membership_id").`is`(membershipId))
        return r2dbcEntityTemplate.update(query, update, MembershipEntity::class.java)
    }

    override fun delete(
        membershipId: UUID?,
        roomId: UUID?,
        role: RoomRole?
    ): Mono<Long> {
        var criteria = Criteria.empty()
        membershipId?.let { criteria = criteria.and("membership_id").`is`(it) }
        roomId?.let { criteria = criteria.and("room_id").`is`(it) }
        role?.let { criteria = criteria.and("role").`is`(it.name) }

        val query = Query.query(criteria)
        return r2dbcEntityTemplate.delete(query, MembershipEntity::class.java)
    }

    override fun select(
        membershipId: UUID?,
        roomId: UUID?,
        roles: Array<RoomRole>
    ): Flux<Membership> {
        var criteria = Criteria.empty()

        membershipId?.let { criteria = criteria.and("membership_id").`is`(it) }
        roomId?.let { criteria = criteria.and("room_id").`is`(it) }
        if (roles.isNotEmpty()) {
            criteria = criteria.and("role").`in`(roles.map { it.name })
        }

        if (criteria.isEmpty) return Flux.error(IllegalArgumentException("All select arguments are null"))

        val query = Query.query(criteria)

        return r2dbcEntityTemplate
            .select(query, MembershipEntity::class.java)
            .map { it.toDomain() }
    }
}