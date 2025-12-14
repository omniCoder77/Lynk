package com.ethyllium.roomservice.application.service

import com.ethyllium.roomservice.domain.exception.InvalidRoomActionException
import com.ethyllium.roomservice.domain.exception.UnauthorizedRoomActionException
import com.ethyllium.roomservice.domain.model.BannedUser
import com.ethyllium.roomservice.domain.model.RoomRole
import com.ethyllium.roomservice.domain.port.driven.BannedUserRepository
import com.ethyllium.roomservice.domain.port.driven.MembershipRepository
import com.ethyllium.roomservice.domain.port.driver.BannedService
import com.ethyllium.roomservice.infrastructure.util.UUIDUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

@Service
class BannedServiceImpl(
    private val transactionalOperator: TransactionalOperator,
    private val membershipRepository: MembershipRepository,
    private val bannedUserRepository: BannedUserRepository
) : BannedService {

    override fun ban(
        bannerId: UUID,
        roomId: UUID,
        bannedUserId: UUID,
        reason: String?,
        bannedUntil: Instant?
    ): Mono<Void> {
        if (bannerId == bannedUserId) {
            return Mono.error(InvalidRoomActionException("User cannot ban themselves"))
        }

        return transactionalOperator.execute {
            membershipRepository.select(membershipId = bannerId, roomId = roomId).next()
                .switchIfEmpty(Mono.error(UnauthorizedRoomActionException("Banner is not a member of this room")))
                .flatMap { bannerMembership ->
                    if (bannerMembership.role.priority < RoomRole.MODERATOR.priority) {
                        return@flatMap Mono.error(UnauthorizedRoomActionException("Insufficient permissions to ban users"))
                    }

                    membershipRepository.select(membershipId = bannedUserId, roomId = roomId).next()
                        .map { it.role }
                        .defaultIfEmpty(RoomRole.NON_MEMBER)
                        .flatMap { targetRole ->
                            if (targetRole != RoomRole.NON_MEMBER && bannerMembership.role.priority <= targetRole.priority) {
                                return@flatMap Mono.error(UnauthorizedRoomActionException("Cannot ban a user with equal or higher priority"))
                            }

                            val bannedId = UUIDUtils.merge(bannedUserId.toString(), roomId.toString())

                            val banEntry = BannedUser(
                                bannedId = bannedId,
                                userId = bannedUserId,
                                roomId = roomId,
                                reason = reason ?: "Banned by staff",
                                bannedAt = Instant.now(),
                                bannedUntil = bannedUntil
                            )

                            val processBan = bannedUserRepository.insert(banEntry)

                            if (targetRole != RoomRole.NON_MEMBER) {
                                membershipRepository.delete(membershipId = bannedUserId, roomId = roomId, role = null)
                                    .then(processBan)
                            } else {
                                processBan
                            }
                        }
                }
        }.then()
    }

    override fun unban(unbannerId: UUID, roomId: UUID, bannedUserId: UUID): Mono<Boolean> {
        return membershipRepository.select(membershipId = unbannerId, roomId = roomId).next()
            .switchIfEmpty(Mono.error(UnauthorizedRoomActionException("Unbanner is not a member of this room")))
            .flatMap { membership ->
                if (membership.role.priority < RoomRole.MODERATOR.priority) {
                    return@flatMap Mono.error(UnauthorizedRoomActionException("Insufficient permissions to unban users"))
                }
                val bannedId = UUIDUtils.merge(bannedUserId.toString(), roomId.toString())
                bannedUserRepository.delete(bannedId).map { it > 0 }
            }
    }
}