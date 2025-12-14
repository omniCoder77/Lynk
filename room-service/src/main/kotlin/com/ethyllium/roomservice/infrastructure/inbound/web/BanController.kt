package com.ethyllium.roomservice.infrastructure.inbound.web

import com.ethyllium.roomservice.domain.port.driver.BannedService
import com.ethyllium.roomservice.infrastructure.outbound.security.LynkAuthenticationToken
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.*

@RestController
@RequestMapping("/ban")
class BanController(private val bannedService: BannedService) {

    @PostMapping("/{roomId}/{bannedUserId}")
    fun ban(@PathVariable bannedUserId: UUID, @PathVariable roomId: UUID, authenticationToken: LynkAuthenticationToken): Mono<ResponseEntity<String>> {
        val bannerId = UUID.fromString(authenticationToken.userId)
        return bannedService.ban(bannerId, roomId, bannedUserId)
            .then(Mono.just(ResponseEntity.ok("User banned successfully")))
    }

    @DeleteMapping("/{roomId}/{bannedUserId}")
    fun unban(@PathVariable bannedUserId: UUID, @PathVariable roomId: UUID, authenticationToken: LynkAuthenticationToken): Mono<ResponseEntity<String>> {
        val unbannerId = UUID.fromString(authenticationToken.userId)
        return bannedService.unban(unbannerId, roomId, bannedUserId)
            .flatMap {
                if(it) Mono.just(ResponseEntity.ok("User unbanned successfully"))
                else Mono.just(ResponseEntity.notFound().build())
            }
    }
}