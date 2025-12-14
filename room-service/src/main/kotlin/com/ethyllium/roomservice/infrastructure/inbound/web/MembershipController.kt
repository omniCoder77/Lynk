package com.ethyllium.roomservice.infrastructure.inbound.web

import com.ethyllium.roomservice.domain.port.driver.MembershipService
import com.ethyllium.roomservice.infrastructure.outbound.security.LynkAuthenticationToken
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@RequestMapping("/memberships")
class MembershipController(private val membershipService: MembershipService) {

    @PostMapping("/{roomId}")
    fun join(@PathVariable roomId: UUID, authenticationToken: LynkAuthenticationToken): Mono<ResponseEntity<String>> {
        val joinerId = UUID.fromString(authenticationToken.userId)
        return membershipService.join(roomId, joinerId)
            .then(Mono.fromCallable { ResponseEntity.ok().body("User $joinerId successfully joined!") })
    }
}