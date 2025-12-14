package com.ethyllium.roomservice.application.service

import com.ethyllium.roomservice.domain.exception.UnauthorizedRoomActionException
import com.ethyllium.roomservice.domain.model.Membership
import com.ethyllium.roomservice.domain.model.RoomRole
import com.ethyllium.roomservice.domain.port.driven.BannedUserRepository
import com.ethyllium.roomservice.domain.port.driven.MembershipRepository
import com.ethyllium.roomservice.domain.port.driver.MembershipService
import com.ethyllium.roomservice.infrastructure.util.UUIDUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
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
                .flatMap {
                    Mono.error<Void>(
                        UnauthorizedRoomActionException(
                            "User $joinerId is not allowed to join $roomId"
                        )
                    )
                }
                .switchIfEmpty(
                    Mono.defer {
                        val membership = Membership(
                            userId = joinerId,
                            roomId = roomId,
                            role = RoomRole.MEMBER
                        )
                        membershipRepository.insert(membership)
                    }
                )
        }.then()
    }
}