package com.ethyllium.roomservice.domain.port.driver

import reactor.core.publisher.Mono
import java.util.UUID

interface MembershipService {
    fun join(roomId: UUID, joinerId: UUID): Mono<Void>
}