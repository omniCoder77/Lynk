package com.ethyllium.userservice.infrastructure.outbound.postgres

import com.ethyllium.userservice.domain.model.Member
import com.ethyllium.userservice.domain.model.MemberRole
import com.ethyllium.userservice.domain.port.driven.MemberRepository
import com.ethyllium.userservice.infrastructure.outbound.postgres.entity.MemberEntity
import com.ethyllium.userservice.infrastructure.outbound.postgres.entity.toEntity
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.data.relational.core.query.Update
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.*

@Component
class MemberRepositoryImpl(private val r2dbcEntityTemplate: R2dbcEntityTemplate) : MemberRepository {
    override fun get(memberId: UUID): Mono<Member> {
        val query = Query.query(Criteria.where("member_id").`is`(memberId))
        return r2dbcEntityTemplate.selectOne(query, MemberEntity::class.java).map { it.toModel() }
    }

    override fun store(member: Member): Mono<UUID> {
        return r2dbcEntityTemplate.insert(member.toEntity()).map { it.memberId }
    }

    override fun delete(memberId: UUID): Mono<Boolean> {
        val query = Query.query(Criteria.where("member_id").`is`(memberId))
        return r2dbcEntityTemplate.delete(query, MemberEntity::class.java).map { it > 0 }
    }

    override fun update(
        role: MemberRole?, memberId: UUID, isAllowedToMessage: Boolean?, isAllowedToSendMedia: Boolean?
    ): Mono<Boolean> {
        var update = Update.from(emptyMap())
        if (role != null) update = update.set("role", role.name)
        if (isAllowedToMessage != null) update = update.set("is_allowed_to_message", isAllowedToMessage)
        if (isAllowedToSendMedia != null) update = update.set("is_allowed_to_send_media", isAllowedToSendMedia)
        val query = Query.query(Criteria.where("member_id").`is`(memberId))
        return if (update.assignments.isNotEmpty()) r2dbcEntityTemplate.update(query, update, MemberEntity::class.java).map { it > 0 }
        else Mono.just(false)
    }
}