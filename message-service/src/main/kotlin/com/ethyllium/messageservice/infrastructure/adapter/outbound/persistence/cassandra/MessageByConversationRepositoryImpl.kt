package com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra

import com.ethyllium.messageservice.domain.model.Message
import com.ethyllium.messageservice.domain.port.outbound.MessageByConversationRepository
import com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.entity.MessageByConversationEntity
import com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.entity.toMessageByConversationEntity
import com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.repository.CassandraMessageByConversationRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class MessageByConversationRepositoryImpl(
    private val cassandraMessageByConversationRepository: CassandraMessageByConversationRepository
) : MessageByConversationRepository {
    override fun insert(message: Message): Mono<MessageByConversationEntity> {
        return cassandraMessageByConversationRepository.insert(message.toMessageByConversationEntity())
    }
}