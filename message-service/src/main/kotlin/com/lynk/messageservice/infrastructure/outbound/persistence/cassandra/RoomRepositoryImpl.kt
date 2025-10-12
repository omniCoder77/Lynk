package com.lynk.messageservice.infrastructure.outbound.persistence.cassandra

import com.lynk.messageservice.domain.port.driver.RoomRepository
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.Room
import org.slf4j.LoggerFactory
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate
import org.springframework.data.cassandra.core.query.Query
import org.springframework.data.cassandra.core.query.where
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.*

@Repository
class RoomRepositoryImpl(private val reactiveCassandraTemplate: ReactiveCassandraTemplate) : RoomRepository {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun create(room: Room): Mono<Room> {
        return reactiveCassandraTemplate.insert(room)
            .doOnError { logger.error("Error creating room ${room.roomId}: ${it.message}", it) }
            .onErrorResume { Mono.empty() }
    }

    override fun getById(roomId: UUID): Mono<Room> {
        return reactiveCassandraTemplate.selectOneById(roomId, Room::class.java)
            .doOnError { logger.error("Error fetching room $roomId: ${it.message}", it) }.onErrorResume { Mono.empty() }
    }

    override fun update(room: Room): Mono<Room> {
        return reactiveCassandraTemplate.update(room)
            .doOnError { logger.error("Error updating room ${room.roomId}: ${it.message}", it) }
            .onErrorResume { Mono.empty() }
    }

    override fun authorizedDelete(roomId: UUID, ownerId: UUID): Mono<Boolean> {
        return reactiveCassandraTemplate.delete(
            Query.query(
                where("room_id").`is`(roomId),
                where("creatorId").`is`(ownerId)
            ), Room::class.java
        ).map { true }.doOnError { logger.error("Error deleting room $roomId: ${it.message}", it) }.onErrorReturn(false)
    }
}