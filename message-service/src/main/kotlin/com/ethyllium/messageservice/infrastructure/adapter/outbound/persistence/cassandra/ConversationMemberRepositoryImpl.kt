package com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra

import com.ethyllium.messageservice.domain.port.outbound.ConversationMemberRepository
import com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.entity.ConversationMemberEntity
import org.springframework.data.cassandra.core.ReactiveCassandraOperations
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class ConversationMemberRepositoryImpl(private val reactiveCassandraOperations: ReactiveCassandraOperations) :
    ConversationMemberRepository {
    override fun findByConversationId(conversationId: String): Flux<ConversationMemberEntity> {
        return reactiveCassandraOperations.select(
            "SELECT * FROM conversation_members WHERE conversation_id = $conversationId",
            ConversationMemberEntity::class.java
        )
    }

    override fun insertAll(memberEntities: List<ConversationMemberEntity>): Flux<ConversationMemberEntity> {
        return Flux.fromIterable(memberEntities).flatMap { reactiveCassandraOperations.insert(it)}
    }
}