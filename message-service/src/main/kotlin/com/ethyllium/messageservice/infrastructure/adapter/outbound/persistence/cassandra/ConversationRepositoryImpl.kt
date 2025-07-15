package com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra

import com.ethyllium.messageservice.domain.exception.ConversationNotFoundException
import com.ethyllium.messageservice.domain.model.Conversation
import com.ethyllium.messageservice.domain.model.ConversationId
import com.ethyllium.messageservice.domain.model.ConversationType
import com.ethyllium.messageservice.domain.model.MessageId
import com.ethyllium.messageservice.domain.port.outbound.ConversationRepository
import com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.entity.ConversationEntity
import com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.entity.ConversationMemberEntity
import com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.entity.toConversationEntity
import com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.repository.CassandraConversationRepository
import org.springframework.data.cassandra.core.ReactiveCassandraOperations
import org.springframework.data.cassandra.core.query.Update
import org.springframework.data.cassandra.core.query.query
import org.springframework.data.cassandra.core.query.where
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class ConversationRepositoryImpl(
    private val conversationRepository: CassandraConversationRepository,
    private val reactiveCassandraOperations: ReactiveCassandraOperations,
    private val cassandraConversationRepository: CassandraConversationRepository
) : ConversationRepository {
    override fun findById(conversationId: String, userId: String): Mono<Conversation> {
        return conversationRepository.findById(conversationId).map {
            Conversation(
                conversationId = ConversationId(it.conversationId),
                type = ConversationType.valueOf(it.type),
                lastMessageId = MessageId(it.last_message_id),
                lastMessageReadId = it.last_message_read_id?.let { lastMessageReadId -> MessageId(lastMessageReadId) },
                lastMessageSentId = it.last_message_sent_id?.let { lastMessageSendId -> MessageId(lastMessageSendId) },
                createdAt = it.created_at,
                updatedAt = it.updated_at
            )
        }
    }

    override fun insert(conversation: Conversation): Mono<ConversationEntity> {
        return conversationRepository.insert(conversation.toConversationEntity())
    }

    override fun update(
        conversationId: String, columnName: String, value: Any
    ): Mono<Boolean> {
        return reactiveCassandraOperations.update(
            query(where("conversation_id").`is`(conversationId)),
            Update.update(columnName, value),
            ConversationEntity::class.java
        )

    }

    override fun addMember(userId: String, conversationId: String): Mono<Boolean> {
        return cassandraConversationRepository.findById(conversationId).switchIfEmpty(
            Mono.error(ConversationNotFoundException(conversationId))
        ).flatMap {
            val update = Update.empty().addTo("members").append(userId)
            reactiveCassandraOperations.update(
                query(where("conversation_id").`is`(conversationId)), update, ConversationEntity::class.java
            )
        }
    }

    override fun removeMember(
        userId: String, conversationId: String
    ): Mono<Boolean> {
        return cassandraConversationRepository.findById(conversationId).switchIfEmpty(
            Mono.error(ConversationNotFoundException(conversationId))
        ).flatMap {
            val update = Update.empty().remove("members", userId)
            reactiveCassandraOperations.update(
                query(where("conversation_id").`is`(conversationId)), update, ConversationEntity::class.java
            )
        }
    }

    override fun removeMemberPostValidation(
        userId: String, conversationId: String, targetUserId: String, validation: (String, Set<String>) -> Unit
    ): Mono<Boolean> {
        val conversationMember = reactiveCassandraOperations.select(
            "SELECT * FROM conversation_members WHERE conversation_id = $conversationId",
            ConversationMemberEntity::class.java
        ).collectList()
        val conversation = cassandraConversationRepository.findById(conversationId).switchIfEmpty(
            Mono.error(ConversationNotFoundException(conversationId))
        )
        return Mono.zip(conversationMember, conversation).flatMap {
            validation(targetUserId, it.t1.mapTo(mutableSetOf()) { member -> member.key.userId })
            val update = Update.empty().remove("members", userId)
            reactiveCassandraOperations.update(
                query(where("conversation_id").`is`(conversationId)), update, ConversationEntity::class.java
            )
        }

    }
}