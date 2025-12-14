package com.ethyllium.roomservice.application.service

import com.ethyllium.roomservice.domain.exception.InvalidRoomActionException
import com.ethyllium.roomservice.domain.exception.UnauthorizedRoomActionException
import com.ethyllium.roomservice.domain.model.Membership
import com.ethyllium.roomservice.domain.model.Room
import com.ethyllium.roomservice.domain.model.RoomRole
import com.ethyllium.roomservice.domain.model.Visibility
import com.ethyllium.roomservice.domain.port.driven.MembershipRepository
import com.ethyllium.roomservice.domain.port.driven.RoomRepository
import com.ethyllium.roomservice.domain.port.driver.RoomService
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

@Service
class RoomServiceImpl(
    private val roomRepository: RoomRepository,
    private val membershipRepository: MembershipRepository,
    private val transactionalOperator: TransactionalOperator
) : RoomService {
    override fun create(room: Room, creatorId: UUID): Mono<Boolean> {
        val membership =
            Membership(userId = creatorId, roomId = room.roomId, joinedAt = Instant.now(), role = RoomRole.ADMIN)
        return transactionalOperator.execute {
            roomRepository.insert(room)
                .then(membershipRepository.insert(membership))
                .thenReturn(true)
        }.single()
    }

    override fun update(
        updaterId: UUID,
        roomName: String?,
        roomId: UUID,
        maxSize: Int?,
        visibility: Visibility?
    ): Mono<Boolean> {
        return membershipRepository.select(
            membershipId = updaterId,
            roomId = roomId,
            roles = arrayOf(RoomRole.ADMIN, RoomRole.MODERATOR)
        )
            .next() // constraint of unique (membershipId and roomId) is applied on database
            .switchIfEmpty(
                Mono.error(
                    UnauthorizedRoomActionException("User does not have permission to update this room")
                )
            )
            .flatMap {
                roomRepository.update(roomName, maxSize, visibility, roomId)
            }.map { it > 0 }
    }

    override fun delete(deleterId: UUID, roomId: UUID): Mono<Boolean> {
        return membershipRepository.select(membershipId = deleterId, roomId = roomId, roles = arrayOf(RoomRole.ADMIN))
            .next()
            .switchIfEmpty(Mono.error(UnauthorizedRoomActionException("User is not authorized to delete this room")))
            .flatMap {
                transactionalOperator.execute {
                    membershipRepository.delete(roomId = roomId, role = null)
                        .then(roomRepository.delete(roomId))
                        .map { it > 0 }
                }.single()
            }
    }

    override fun get(roomId: UUID): Mono<Room> {
        return roomRepository.select(roomId)
            .switchIfEmpty(Mono.error(InvalidRoomActionException("Room not found")))
    }
}