package com.ethyllium.roomservice.infrastructure.outbound.postgres

import com.ethyllium.roomservice.domain.model.Room
import com.ethyllium.roomservice.domain.model.Visibility
import com.ethyllium.roomservice.domain.port.driven.RoomRepository
import com.ethyllium.roomservice.infrastructure.outbound.postgres.entity.RoomEntity
import com.ethyllium.roomservice.infrastructure.outbound.postgres.entity.toEntity
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.data.relational.core.query.Update
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.*

@Repository
class RoomRepositoryImpl(
    private val r2dbcEntityTemplate: R2dbcEntityTemplate
) : RoomRepository {
    override fun insert(room: Room): Mono<Void> =
        r2dbcEntityTemplate.insert(room.toEntity()).then()

    override fun update(
        name: String?,
        maxSize: Int?,
        visibility: Visibility?,
        roomId: UUID
    ): Mono<Long> {
        var update = Update.from(emptyMap())
        name?.let { update = update.set("name", it) }
        maxSize?.let { update = update.set("max_size", it) }
        visibility?.let { update = update.set("visibility", it) }

        val query = Query.query(Criteria.where("room_id").`is`(roomId))
        return r2dbcEntityTemplate.update(query, update, RoomEntity::class.java)
    }

    override fun delete(roomId: UUID): Mono<Long> {
        val query = Query.query(Criteria.where("room_id").`is`(roomId))
        return r2dbcEntityTemplate.delete(query, RoomEntity::class.java)
    }

    override fun select(roomId: UUID): Mono<Room> {
        val query = Query.query(Criteria.where("room_id").`is`(roomId))
        return r2dbcEntityTemplate
            .selectOne(query, RoomEntity::class.java)
            .map { it.toDomain() }
    }
}
