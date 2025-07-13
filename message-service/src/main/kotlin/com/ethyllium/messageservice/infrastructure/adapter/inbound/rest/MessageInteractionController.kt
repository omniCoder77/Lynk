package com.ethyllium.messageservice.infrastructure.adapter.inbound.rest

import com.ethyllium.messageservice.application.port.outbound.MessageInteractionService
import com.ethyllium.messageservice.infrastructure.adapter.inbound.rest.dto.ReactionAction
import com.ethyllium.messageservice.infrastructure.adapter.inbound.rest.dto.ReactionRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/messages")
class MessageInteractionController(
    private val messageInteractionService: MessageInteractionService
) {

    @PostMapping("/{messageId}/react")
    fun handleReaction(
        @RequestHeader("X-User-Id") userId: String,
        @PathVariable messageId: String,
        @RequestBody request: ReactionRequest,
        @RequestParam createAt: Long
    ): Mono<ResponseEntity<Boolean>> = when (request.action) {
        ReactionAction.ADD -> messageInteractionService.addReaction(userId, messageId, request.emoji, createAt)
            .map { ResponseEntity.ok(it) }

        ReactionAction.REMOVE -> messageInteractionService.removeReaction(userId, messageId, request.emoji, createAt)
            .map { ResponseEntity.ok(it) }

    }

    @PostMapping("/mark-read")
    fun markMessagesRead(
        @RequestHeader("X-User-Id") userId: String, @RequestBody request: ReactionRequest, @RequestParam createAt: Long
    ): Mono<ResponseEntity<Boolean>> =
        messageInteractionService.markMessagesRead(userId, request.messageId, createAt).map { ResponseEntity.ok(it) }
}
