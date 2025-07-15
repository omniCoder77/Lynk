package com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra

import com.ethyllium.messageservice.domain.model.Message
import com.ethyllium.messageservice.domain.port.outbound.MessageByConversationRepository
import com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.entity.MessageByConversationEntity
import com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.entity.toMessageByConversationEntity
import org.springframework.data.cassandra.core.ReactiveCassandraOperations
import org.springframework.data.cassandra.core.query.Criteria.where
import org.springframework.data.cassandra.core.query.Query.query
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class MessageByConversationRepositoryImpl(
    private val reactiveCassandraOperations: ReactiveCassandraOperations
) : MessageByConversationRepository {
    override fun insert(message: Message): Mono<MessageByConversationEntity> {
        return reactiveCassandraOperations.insert(message.toMessageByConversationEntity())
    }

    override fun findByMessageId(messageId: String): Mono<MessageByConversationEntity> {
        // NOTE: This requires a secondary index on message_id or a different query strategy
        // For now, we will create a secondary index.
        val query = query(where("message_id").`is`(messageId)).withAllowFiltering()
        return reactiveCassandraOperations.selectOne(query, MessageByConversationEntity::class.java)
    }
}