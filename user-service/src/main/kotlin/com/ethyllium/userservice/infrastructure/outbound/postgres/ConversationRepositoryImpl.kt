package com.ethyllium.userservice.infrastructure.outbound.postgres

import com.ethyllium.userservice.domain.exception.ConversationAlreadyExists
import com.ethyllium.userservice.domain.model.Conversation
import com.ethyllium.userservice.domain.port.driven.ConversationRepository
import com.ethyllium.userservice.infrastructure.outbound.postgres.entity.ConversationEntity
import com.ethyllium.userservice.infrastructure.outbound.postgres.entity.toEntity
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Component
class ConversationRepositoryImpl(private val r2dbcEntityTemplate: R2dbcEntityTemplate) : ConversationRepository {
    override fun insert(conversation: Conversation): Mono<UUID> {
        return r2dbcEntityTemplate.insert(conversation.toEntity()).map { it.conversationId }.onErrorMap {
            when (it) {
                is DataIntegrityViolationException ->
                    ConversationAlreadyExists("Failed to insert conversation due to data integrity violation")
                else -> it
            }
        }
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

    override fun findByUserId(userId: UUID): Flux<Conversation> {
        val query = Query.query(Criteria.where("sender_id").`is`(userId).or(Criteria.where("recipient_id").`is`(userId)))
        return r2dbcEntityTemplate.select(query, ConversationEntity::class.java).map { it.toConversation() }
    }
}