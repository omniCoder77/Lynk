package com.ethyllium.roomservice.application.service

import com.ethyllium.roomservice.domain.exception.InvalidRoomActionException
import com.ethyllium.roomservice.domain.exception.RoomMinimumAdminConstraintException
import com.ethyllium.roomservice.domain.exception.UnauthorizedRoomActionException
import com.ethyllium.roomservice.domain.model.Membership
import com.ethyllium.roomservice.domain.model.RoomRole
import com.ethyllium.roomservice.domain.port.driven.BannedUserRepository
import com.ethyllium.roomservice.domain.port.driven.MembershipRepository
import com.ethyllium.roomservice.domain.port.driver.MembershipService
import com.ethyllium.roomservice.infrastructure.util.UUIDUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Service
class MembershipServiceImpl(
    private val bannedUserRepository: BannedUserRepository,
    private val membershipRepository: MembershipRepository,
    private val transactionalOperator: TransactionalOperator
) : MembershipService {
    override fun join(roomId: UUID, joinerId: UUID): Mono<Void> {
        val bannedId = UUIDUtils.merge(roomId.toString(), joinerId.toString())
        return transactionalOperator.execute {
            bannedUserRepository.select(bannedId)
                .flatMap<Void> {
                    Mono.error(UnauthorizedRoomActionException("User $joinerId is not allowed to join $roomId"))
                }
                .switchIfEmpty(
                    membershipRepository
                        .select(membershipId = joinerId, roomId = roomId).next()
                        .flatMap {
                            Mono.error(UnauthorizedRoomActionException("User $joinerId is already a member"))
                        }
                )
                .switchIfEmpty(
                    Mono.defer {
                        val membership = Membership(userId = joinerId, roomId = roomId, role = RoomRole.MEMBER)
                        membershipRepository.insert(membership)
                    }
                )
        }.then()
    }

    override fun leave(leaverId: UUID, roomId: UUID): Mono<Boolean> {
        return transactionalOperator.execute {
            membershipRepository.select(membershipId = leaverId, roomId = roomId)
                .next()
                .switchIfEmpty(Mono.error(UnauthorizedRoomActionException("User is not a member of this room")))
                .flatMap { membership ->
                    if (membership.role == RoomRole.ADMIN) {
                        membershipRepository.select(membershipId = null, roomId = roomId, roles = arrayOf(RoomRole.ADMIN))
                            .count()
                            .flatMap { count ->
                                if (count > 1) {
                                    membershipRepository.delete(membershipId = leaverId, roomId = roomId, role = null)
                                } else {
                                    Mono.error(RoomMinimumAdminConstraintException("You are the last admin. Assign another admin before leaving."))
                                }
                            }
                    } else {
                        membershipRepository.delete(membershipId = leaverId, roomId = roomId, role = null)
                    }
                }
                .map { it > 0 }
        }.single()
    }

    override fun kick(kickerId: UUID, kickedUserId: UUID, roomId: UUID): Mono<Boolean> {
        if (kickerId.toString() == kickedUserId.toString()) {
            return Mono.error(InvalidRoomActionException("User cannot perform this action on themselves"))
        }
        return Mono.zip(
            membershipRepository.select(membershipId = kickerId, roomId = roomId).next(),
            membershipRepository.select(membershipId = kickedUserId, roomId = roomId).next()
        ).switchIfEmpty(Mono.error(UnauthorizedRoomActionException("One or both users are not in the room")))
            .flatMap { tuple ->
                val kickerMembership = tuple.t1
                val kickedMembership = tuple.t2

                if (kickerMembership.role.priority > kickedMembership.role.priority) {
                    membershipRepository.delete(membershipId = kickedUserId, roomId = roomId, role = null)
                        .map { it > 0 }
                } else {
                    Mono.error(UnauthorizedRoomActionException("Insufficient permissions to kick this user"))
                }
            }
    }

    override fun getMembers(roomId: UUID): Flux<Membership> {
        return membershipRepository.select(membershipId = null, roomId = roomId)
    }

    override fun getUserRooms(userId: UUID): Flux<Membership> {
        return membershipRepository.select(membershipId = userId, roomId = null)
    }
}