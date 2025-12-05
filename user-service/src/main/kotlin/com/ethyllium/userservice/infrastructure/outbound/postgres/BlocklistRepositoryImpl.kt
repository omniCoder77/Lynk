package com.ethyllium.userservice.infrastructure.outbound.postgres

import com.ethyllium.userservice.domain.model.Blocklist
import com.ethyllium.userservice.domain.port.driven.BlocklistRepository
import com.ethyllium.userservice.infrastructure.outbound.postgres.entity.BlocklistEntity
import com.ethyllium.userservice.infrastructure.outbound.postgres.entity.toEntity
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class BlocklistRepositoryImpl(private val r2dbcEntityTemplate: R2dbcEntityTemplate) : BlocklistRepository {
    override fun getBlocklists(userId: UUID): Flux<Blocklist> {
        val query = Query.query(Criteria.where("user_id").`is`(userId))
        return r2dbcEntityTemplate.select(query, BlocklistEntity::class.java).map { it.toModel() }
    }

    override fun getBlocklistById(blocklistId: UUID): Mono<Blocklist> {
        val query = Query.query(Criteria.where("blocklist_id").`is`(blocklistId))
        return r2dbcEntityTemplate.selectOne(query, BlocklistEntity::class.java).map { it.toModel() }
    }

    override fun insert(blocklist: Blocklist): Mono<UUID> {
        return r2dbcEntityTemplate.insert<BlocklistEntity>(blocklist.toEntity()).map { it.blocklistId }
    }

    override fun delete(blocklistId: UUID): Mono<Boolean> {
        val query = Query.query(Criteria.where("blocklist_id").`is`(blocklistId))
        return r2dbcEntityTemplate.delete(query, BlocklistEntity::class.java).map { it > 0 }
    }
}