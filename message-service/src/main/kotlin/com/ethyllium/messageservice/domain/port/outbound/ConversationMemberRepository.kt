package com.ethyllium.messageservice.domain.port.outbound

import com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.entity.ConversationMemberEntity
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ConversationMemberRepository {
    fun findByConversationId(conversationId: String): Flux<ConversationMemberEntity>
    fun insertAll(memberEntities: List<ConversationMemberEntity>): Flux<ConversationMemberEntity>
}