package com.ethyllium.userservice.application.service

import com.ethyllium.userservice.domain.exception.UserNotFoundException
import com.ethyllium.userservice.domain.model.Blocklist
import com.ethyllium.userservice.domain.model.Conversation
import com.ethyllium.userservice.domain.port.driven.BlocklistRepository
import com.ethyllium.userservice.domain.port.driven.ConversationRepository
import com.ethyllium.userservice.domain.port.driven.UserRepository
import com.ethyllium.userservice.domain.port.driver.ConversationService
import com.ethyllium.userservice.infrastructure.util.UUIDUtils
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

@Service
class ConversationServiceImpl(
    private val blocklistRepository: BlocklistRepository,
    private val userRepository: UserRepository,
    private val conversationRepository: ConversationRepository
) : ConversationService {
    override fun block(
        userId: UUID,
        blockedUserUuid: UUID
    ): Mono<UUID> {
        val blocklistId = UUIDUtils.merge(userId.toString(), blockedUserUuid.toString())
        val blocklist = Blocklist(blocklistId, userId, blockedUserUuid, Instant.now())
        return userRepository.exist(blockedUserUuid).flatMap { exists ->
            if (exists) {
                blocklistRepository.insert(blocklist)
            } else {
                Mono.error(UserNotFoundException("User with id $blockedUserUuid not found"))
            }
        }
    }

    override fun unblock(userId: UUID, blockedUserUuid: UUID): Mono<Boolean> {
        val blocklistId = UUIDUtils.merge(userId.toString(), blockedUserUuid.toString())
        return userRepository.exist(blockedUserUuid).flatMap { exists ->
            if (exists) {
                blocklistRepository.delete(blocklistId)
            } else {
                Mono.error(UserNotFoundException("User with id $blockedUserUuid not found"))
            }
        }
    }

    override fun createConversationForUser(user1Id: UUID, user2Id: UUID): Mono<UUID> {
        val conversationId = UUIDUtils.merge(user1Id.toString(), user2Id.toString())
        val conversation = Conversation(conversationId, user1Id, user2Id)
        return userRepository.exist(user2Id).flatMap { exists ->
            if (exists) {
                conversationRepository.insert(conversation)
            } else {
                Mono.error(UserNotFoundException("User with id $user2Id not found"))
            }
        }
    }
    override fun getConversationsForUser(userId: UUID): Flux<Conversation> {
        return conversationRepository.findByUserId(userId)
    }

    override fun delete(userId: UUID, recipientId: UUID): Mono<Boolean> {
        val conversationId = UUIDUtils.merge(userId.toString(), recipientId.toString())
        return conversationRepository.delete(conversationId)
    }
}