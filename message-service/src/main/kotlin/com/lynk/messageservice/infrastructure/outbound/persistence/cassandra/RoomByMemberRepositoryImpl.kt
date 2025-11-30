package com.lynk.messageservice.infrastructure.outbound.persistence.cassandra

import com.lynk.messageservice.domain.model.Room
import com.lynk.messageservice.domain.port.driver.RoomByMemberRepository
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.RoomByMember
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.RoomByMemberKey
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate
import org.springframework.data.cassandra.core.query.Criteria
import org.springframework.data.cassandra.core.query.Query
import org.springframework.data.cassandra.core.query.Update
import org.springframework.data.cassandra.core.query.where
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Repository
class RoomByMemberRepositoryImpl(private val reactiveCassandraTemplate: ReactiveCassandraTemplate) :
    RoomByMemberRepository {
    override fun createRoomByMember(memberId: UUID, roomId: UUID, name: String, avatarExtension: String?): Mono<UUID> {
        val roomByMember = RoomByMember(
            roomByMemberKey = RoomByMemberKey(memberId = memberId, roomId = roomId),
            name = name
        )
        return reactiveCassandraTemplate.insert(roomByMember).map { it.roomByMemberKey.roomId }
    }

    override fun updateRoomByMember(
        memberId: UUID,
        roomId: UUID,
        name: String?
    ): Mono<Boolean> {

        var update = Update.empty()
        var hasUpdates = false

        if (name != null) {
            update = update.set("name", name)
            hasUpdates = true
        }

        if (!hasUpdates) {
            return Mono.just(true)
        }

        val query = Query.query(where("member_id").`is`(memberId), where("room_id").`is`(roomId))

        return reactiveCassandraTemplate.update(query, update, RoomByMember::class.java)
    }

    override fun getRooms(memberId: UUID): Flux<Room> {
        return reactiveCassandraTemplate.select(
            Query.query(Criteria.where("member_id").`is`(memberId)),
            RoomByMember::class.java
        ).map { it.toDomain() }
    }
}