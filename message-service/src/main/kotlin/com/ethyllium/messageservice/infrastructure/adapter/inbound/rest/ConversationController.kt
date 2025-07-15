package com.ethyllium.messageservice.infrastructure.adapter.inbound.rest

import com.ethyllium.messageservice.application.port.outbound.ConversationMembershipService
import com.ethyllium.messageservice.domain.port.inbound.MessageService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/conversations")
class ConversationController(
    private val messageService: MessageService, private val conversationMembershipService: ConversationMembershipService
) {

    data class CreateGroupRequest(val name: String, val members: Set<String>)

    @PostMapping("/group")
    fun createGroup(
        @RequestHeader("X-User-Id") creatorId: String, @RequestBody request: CreateGroupRequest
    ): Mono<Map<String, String>> {
        return messageService.createGroupConversation(creatorId, request.name, request.members)
            .map { mapOf("conversationId" to it.conversationId.value) }
    }

    @PostMapping("/{conversationId}/join")
    fun joinConversation(
        @RequestHeader("X-User-Id") userId: String, @PathVariable conversationId: String
    ): Mono<ResponseEntity<Boolean>> =
        conversationMembershipService.joinConversation(userId, conversationId).map { ResponseEntity.ok(it) }

    @PostMapping("/{conversationId}/leave")
    fun leaveConversation(
        @RequestHeader("X-User-Id") userId: String, @PathVariable conversationId: String
    ): Mono<ResponseEntity<Boolean>> =
        conversationMembershipService.leaveConversation(userId, conversationId).map { ResponseEntity.ok(it) }

    @PostMapping("/{conversationId}/kick")
    fun kickUser(
        @RequestHeader("X-User-Id") performingUserId: String,
        @PathVariable conversationId: String,
        @RequestParam targetUserId: String
    ): Mono<ResponseEntity<Boolean>> =
        conversationMembershipService.kickUser(performingUserId, conversationId, targetUserId)
            .map { ResponseEntity.ok(it) }
}