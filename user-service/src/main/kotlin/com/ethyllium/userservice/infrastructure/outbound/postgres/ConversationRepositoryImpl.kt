package com.ethyllium.userservice.infrastructure.outbound.postgres

import com.ethyllium.userservice.domain.model.Conversation
import com.ethyllium.userservice.domain.port.driven.ConversationRepository
import com.ethyllium.userservice.infrastructure.outbound.postgres.entity.ConversationEntity
import com.ethyllium.userservice.infrastructure.outbound.postgres.entity.toEntity
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class ConversationRepositoryImpl(private val r2dbcEntityTemplate: R2dbcEntityTemplate) : ConversationRepository {
    override fun store(conversation: Conversation): Mono<ConversationEntity> {
        return r2dbcEntityTemplate.insert(conversation.toEntity())
    }

    override fun delete(conversationId: UUID): Mono<Boolean> {
        val query = Query.query(Criteria.where("conversation_id").`is`(conversationId))
        return r2dbcEntityTemplate.delete(query, ConversationEntity::class.java).map { it > 0 }
    }

    override fun select(conversationId: UUID): Mono<Conversation> {
        val query = Query.query(Criteria.where("conversation_id").`is`(conversationId))
        return r2dbcEntityTemplate.selectOne(query, ConversationEntity::class.java).map { it.toConversation() }
    }

    override fun exists(conversationId: UUID): Mono<Boolean> {
        val query = Query.query(Criteria.where("conversation_id").`is`(conversationId))
        return r2dbcEntityTemplate.exists(query, ConversationEntity::class.java)
    }
}