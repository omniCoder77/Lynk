package com.ethyllium.userservice.infrastructure.outbound.postgres

import com.ethyllium.userservice.domain.model.Room
import com.ethyllium.userservice.domain.port.driven.RoomRepository
import com.ethyllium.userservice.infrastructure.outbound.postgres.entity.RoomEntity
import com.ethyllium.userservice.infrastructure.outbound.postgres.entity.toEntity
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class RoomRepositoryImpl(private val r2dbcEntityTemplate: R2dbcEntityTemplate) : RoomRepository {
    override fun store(room: Room): Mono<UUID> {
        return r2dbcEntityTemplate.insert(room.toEntity()).map { it.roomId }
    }

    override fun delete(roomId: UUID): Mono<Boolean> {
        val query = Query.query(Criteria.where("room_id").`is`(roomId))
        return r2dbcEntityTemplate.delete(query, RoomEntity::class.java).map { it > 0 }
    }

    override fun get(roomId: UUID): Mono<Room> {
        val query = Query.query(Criteria.where("room_id").`is`(roomId))
        return r2dbcEntityTemplate.selectOne(query, RoomEntity::class.java).map { it.toModel() }
    }
}