package com.ethyllium.roomservice.infrastructure.outbound.postgres

import com.ethyllium.roomservice.domain.exception.RoomAlreadyExistsException
import com.ethyllium.roomservice.domain.model.Room
import com.ethyllium.roomservice.domain.model.Visibility
import com.ethyllium.roomservice.domain.port.driven.RoomRepository
import com.ethyllium.roomservice.infrastructure.outbound.postgres.entity.RoomEntity
import com.ethyllium.roomservice.infrastructure.outbound.postgres.entity.toEntity
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.data.relational.core.query.Update
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.*

@Repository
class RoomRepositoryImpl(private val r2dbcEntityTemplate: R2dbcEntityTemplate) : RoomRepository {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun insert(room: Room): Mono<Boolean> {
        return r2dbcEntityTemplate.insert(room.toEntity()).map { true }.onErrorResume { error ->
            logger.warn("Error inserting room ${room.name}", error)

            if (error is DataIntegrityViolationException && isDuplicateNameError(error)) {
                Mono.error(RoomAlreadyExistsException("Room with name '${room.name}' already exists"))
            } else {
                Mono.just(false)
            }
        }
    }

    private fun isDuplicateNameError(error: DataIntegrityViolationException): Boolean {
        val message = error.message?.lowercase() ?: ""
        val rootCause = error.rootCause?.message?.lowercase() ?: ""

        return (message.contains("duplicate") && message.contains("name")) || (rootCause.contains("duplicate") && rootCause.contains("name"))    }

    override fun update(
        name: String?, maxSize: Int?, visibility: Visibility?, roomId: UUID
    ): Mono<Boolean> {
        var update = Update.from(emptyMap())
        name?.let { update = update.set("name", it) }
        maxSize?.let { update = update.set("max_size", it) }
        visibility?.let { update = update.set("visibility", it) }
        val query = Query.query(Criteria.where("room_id").`is`(roomId))
        return r2dbcEntityTemplate.update(
            query, update, RoomEntity::class.java
        ).map { it > 0 }
    }

    override fun delete(roomId: UUID): Mono<Boolean> {
        val query = Query.query(Criteria.where("room_id").`is`(roomId))
        return r2dbcEntityTemplate.delete(query, RoomEntity::class.java).map { it > 0 }.onErrorResume {
            logger.warn("Error deleting room $roomId", it)
            Mono.just(false)
        }
    }

    override fun select(roomId: UUID): Mono<Room> {
        val query = Query.query(Criteria.where("room_id").`is`(roomId))
        return r2dbcEntityTemplate.selectOne(
            query, RoomEntity::class.java
        ).map { it.toDomain() }
    }
}