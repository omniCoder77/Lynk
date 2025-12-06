package com.ethyllium.userservice.infrastructure.inbound.web

import com.ethyllium.userservice.domain.exception.ConversationAlreadyExists
import com.ethyllium.userservice.domain.exception.UserAlreadyBlockedException
import com.ethyllium.userservice.domain.exception.UserNotFoundException
import com.ethyllium.userservice.domain.model.Conversation
import com.ethyllium.userservice.domain.port.driver.ConversationService
import com.ethyllium.userservice.infrastructure.inbound.web.dto.CreateConversationRequest
import com.ethyllium.userservice.infrastructure.outbound.security.LynkAuthenticationToken
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.*

@RestController
@RequestMapping("/conversation")
class ConversationController(private val conversationService: ConversationService) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @PatchMapping("/block/{blockedUserId}")
    fun block(
        authenticationToken: LynkAuthenticationToken,
        @PathVariable blockedUserId: String
    ): Mono<ResponseEntity<String>> {
        val userId =
            UUID.fromString(authenticationToken.userId) // Can never be invalid as it's extracted from a valid token.
        val blockedUserUuid = try {
            UUID.fromString(blockedUserId)
        } catch (e: IllegalArgumentException) {
            logger.info("Invalid blocked user UUID: $blockedUserId", e)
            return Mono.just(
                ResponseEntity.badRequest().build()
            )
        }
        return conversationService.block(userId, blockedUserUuid).map {
            ResponseEntity.ok("User $blockedUserId blocked successfully.")
        }.onErrorResume {
            when (it) {
                is UserAlreadyBlockedException -> {
                    logger.info("User $blockedUserId is already blocked by user $userId")
                    Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build())
                }
                is UserNotFoundException -> {
                    logger.info("User $blockedUserId not found in database")
                    Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build())
                }
                else -> {
                    logger.error("Unexpected error: $it")
                    Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
                }
            }
        }
    }

    @PatchMapping("/unblock/{blockedUserId}")
    fun unblock(
        @PathVariable blockedUserId: String,
        authenticationToken: LynkAuthenticationToken
    ): Mono<ResponseEntity<String>> {
        val userId =
            UUID.fromString(authenticationToken.userId) // Can never be invalid as it's extracted from a valid token.
        val blockedUserUuid = try {
            UUID.fromString(blockedUserId)
        } catch (e: IllegalArgumentException) {
            logger.info("Invalid blocked user UUID: $blockedUserId", e)
            return Mono.just(
                ResponseEntity.badRequest().build()
            )
        }

        return conversationService.unblock(userId, blockedUserUuid).map {
            if (it) {
                ResponseEntity.ok("User $blockedUserId unblocked successfully.")
            } else {
                ResponseEntity.status(HttpStatus.CONFLICT).body("Failed to unblock user $blockedUserId.")
            }
        }.onErrorResume {
            when (it) {
                is UserNotFoundException -> Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build())
                else -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
            }
        }
    }

    @PostMapping
    fun createConversation(
        authenticationToken: LynkAuthenticationToken,
        @RequestBody createConversationRequest: CreateConversationRequest // keeping a request body for future extensibility
    ): Mono<ResponseEntity<String>> {
        val user1Id =
            UUID.fromString(authenticationToken.userId) // Can never be invalid as it's extracted from a valid token.
        val user2Id = try {
            UUID.fromString(createConversationRequest.userId)
        } catch (e: IllegalArgumentException) {
            logger.info("Invalid user UUID: ${createConversationRequest.userId}", e)
            return Mono.just(
                ResponseEntity.badRequest().build()
            )
        }
        return conversationService.createConversationForUser(user1Id, user2Id).map { conversationId ->
            ResponseEntity.status(HttpStatus.CREATED).body(conversationId.toString())
        }.onErrorResume {
            when (it) {
                is UserNotFoundException -> Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build())
                is ConversationAlreadyExists -> Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build())
                else -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
            }
        }
    }

    @GetMapping
    fun getConversations(
        authenticationToken: LynkAuthenticationToken
    ): Mono<ResponseEntity<List<Conversation>>> {
        val userId =
            UUID.fromString(authenticationToken.userId) // Can never be invalid as it's extracted from a valid token.

        return conversationService.getConversationsForUser(userId).collectList().map { conversations ->
            ResponseEntity.ok(conversations)
        }.onErrorResume {
            when (it) {
                is UserNotFoundException -> Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build())
                else -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
            }
        }
    }

    @DeleteMapping("/{recipientId}")
    fun deleteConversation(
        authenticationToken: LynkAuthenticationToken,
        @PathVariable recipientId: String
    ): Mono<ResponseEntity<String>> {
        val userId =
            UUID.fromString(authenticationToken.userId) // Can never be invalid as it's extracted from a valid token.
        val conversationUuid = try {
            UUID.fromString(recipientId)
        } catch (e: IllegalArgumentException) {
            logger.info("Invalid conversation UUID: $recipientId", e)
            return Mono.just(
                ResponseEntity.badRequest().build()
            )
        }
        return conversationService.delete(userId, conversationUuid).map {
            if (it) {
                ResponseEntity.ok("Conversation $recipientId deleted successfully.")
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).body("Conversation $recipientId. does not exist")
            }
        }
    }
}