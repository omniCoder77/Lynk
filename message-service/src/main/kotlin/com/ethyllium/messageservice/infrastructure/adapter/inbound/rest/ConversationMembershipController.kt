package com.ethyllium.messageservice.infrastructure.adapter.inbound.rest

import com.ethyllium.messageservice.application.port.outbound.ConversationMembershipService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/conversations")
class ConversationMembershipController(
    private val conversationMembershipService: ConversationMembershipService
) {

    @PostMapping("/{conversationId}/join")
    fun joinConversation(
        @RequestHeader("X-User-Id") userId: String,
        @PathVariable conversationId: String
    ): Mono<ResponseEntity<Boolean>> =
        conversationMembershipService.joinConversation(userId, conversationId)
            .map { ResponseEntity.ok(it) }

    @PostMapping("/{conversationId}/leave")
    fun leaveConversation(
        @RequestHeader("X-User-Id") userId: String,
        @PathVariable conversationId: String
    ): Mono<ResponseEntity<Boolean>> =
        conversationMembershipService.leaveConversation(userId, conversationId)
            .map { ResponseEntity.ok(it) }

    @PostMapping("/{conversationId}/kick")
    fun kickUser(
        @RequestHeader("X-User-Id") userId: String,
        @PathVariable conversationId: String,
        @RequestParam targetUserId: String
    ): Mono<ResponseEntity<Boolean>> =
        conversationMembershipService.kickUser(userId, conversationId, targetUserId)
            .map { ResponseEntity.ok(it) }
}
