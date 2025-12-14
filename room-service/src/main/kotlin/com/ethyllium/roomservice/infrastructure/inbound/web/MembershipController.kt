package com.ethyllium.roomservice.infrastructure.inbound.web

import com.ethyllium.roomservice.domain.model.Membership
import com.ethyllium.roomservice.domain.port.driver.MembershipService
import com.ethyllium.roomservice.infrastructure.outbound.security.LynkAuthenticationToken
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@RestController
@RequestMapping("/memberships")
class MembershipController(private val membershipService: MembershipService) {

    @PostMapping("/{roomId}")
    fun join(@PathVariable roomId: UUID, authenticationToken: LynkAuthenticationToken): Mono<ResponseEntity<String>> {
        val joinerId = UUID.fromString(authenticationToken.userId)
        return membershipService.join(roomId, joinerId)
            .then(Mono.fromCallable { ResponseEntity.ok().body("User $joinerId successfully joined!") })
    }

    @PostMapping("/leave/{roomId}")
    fun leaveRoom(
        @PathVariable roomId: UUID,
        authenticationToken: LynkAuthenticationToken
    ): Mono<ResponseEntity<String>> {
        val joinerId = UUID.fromString(authenticationToken.userId)
        return membershipService.leave(joinerId, roomId).flatMap { Mono.just(ResponseEntity.ok().build()) }
    }

    @PostMapping("/kick/{kickedUserId}/{roomId}")
    fun kick(
        @PathVariable kickedUserId: UUID,
        @PathVariable roomId: UUID,
        authenticationToken: LynkAuthenticationToken
    ): Mono<ResponseEntity<String>> {
        val kickerId = UUID.fromString(authenticationToken.userId)
        return membershipService.kick(kickerId, kickedUserId, roomId)
            .map { ResponseEntity.ok().body("User $kickerId kicked successfully") }
    }

    @GetMapping("/{roomId}/members")
    fun getMembers(@PathVariable roomId: UUID): Flux<Membership> {
        return membershipService.getMembers(roomId)
    }

    @GetMapping("/my-rooms")
    fun getMyRooms(authenticationToken: LynkAuthenticationToken): Flux<Membership> {
        val userId = UUID.fromString(authenticationToken.userId)
        return membershipService.getUserRooms(userId)
    }
}