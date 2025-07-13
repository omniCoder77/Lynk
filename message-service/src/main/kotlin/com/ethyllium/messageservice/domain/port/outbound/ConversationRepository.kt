package com.ethyllium.messageservice.domain.port.outbound

import com.ethyllium.messageservice.domain.model.Conversation
import com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.entity.ConversationEntity
import reactor.core.publisher.Mono

interface ConversationRepository {
    fun findById(conversationId: String, userId: String = ""): Mono<Conversation>
    fun insert(conversation: Conversation): Mono<ConversationEntity>
    fun update(conversationId: String, columnName: String, value: Any): Mono<Boolean>
    fun addMember(userId: String, conversationId: String): Mono<Boolean>
    fun removeMember(userId: String, conversationId: String): Mono<Boolean>
    fun removeMemberPostValidation(userId: String, conversationId: String, targetUserId: String, validation: (String, Set<String>) -> Unit): Mono<Boolean>
}