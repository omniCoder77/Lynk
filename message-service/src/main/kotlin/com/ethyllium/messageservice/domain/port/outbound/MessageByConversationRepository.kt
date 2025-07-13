package com.ethyllium.messageservice.domain.port.outbound

import com.ethyllium.messageservice.domain.model.Message
import com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.entity.MessageByConversationEntity
import reactor.core.publisher.Mono

interface MessageByConversationRepository {
    fun insert(message: Message): Mono<MessageByConversationEntity>
}