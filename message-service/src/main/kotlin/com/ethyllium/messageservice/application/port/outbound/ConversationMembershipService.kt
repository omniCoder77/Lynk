package com.ethyllium.messageservice.application.port.outbound

import reactor.core.publisher.Mono

interface ConversationMembershipService {
    fun joinConversation(userId: String, conversationId: String): Mono<Boolean>
    fun leaveConversation(userId: String, conversationId: String): Mono<Boolean>
    fun kickUser(userId: String, conversationId: String, targetUserId: String): Mono<Boolean>
}