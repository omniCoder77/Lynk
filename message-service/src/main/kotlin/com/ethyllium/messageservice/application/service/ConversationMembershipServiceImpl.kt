package com.ethyllium.messageservice.application.service

import com.ethyllium.messageservice.application.port.outbound.ConversationMembershipService
import com.ethyllium.messageservice.domain.port.outbound.ConversationRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class ConversationMembershipServiceImpl(private val conversationRepository: ConversationRepository) :
    ConversationMembershipService {
    override fun joinConversation(
        userId: String, conversationId: String
    ): Mono<Boolean> {
        return conversationRepository.addMember(userId, conversationId)
    }

    override fun leaveConversation(userId: String, conversationId: String): Mono<Boolean> {
        return conversationRepository.removeMember(userId, conversationId)
    }

    override fun kickUser(
        userId: String, conversationId: String, targetUserId: String
    ): Mono<Boolean> {
        return conversationRepository.removeMemberPostValidation(userId, conversationId, targetUserId) { kickedId, adminsId ->
            if (!adminsId.contains(userId)) throw IllegalArgumentException("User $userId is not an admin of conversation $conversationId")
        }
    }
}