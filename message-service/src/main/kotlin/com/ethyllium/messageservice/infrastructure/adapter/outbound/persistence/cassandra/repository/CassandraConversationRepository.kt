package com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.repository

import com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.entity.ConversationEntity
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository
import org.springframework.stereotype.Repository

@Repository
interface CassandraConversationRepository: ReactiveCassandraRepository<ConversationEntity, String> {
}