package com.lynk.messageservice.infrastructure.outbound.persistence.cassandra

import com.lynk.messageservice.domain.port.driver.ConversationByUserRepository
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.ConversationByUser
import com.lynk.messageservice.infrastructure.util.UUIDUtils
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate
import org.springframework.data.cassandra.core.query.Query
import org.springframework.data.cassandra.core.query.where
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Repository
class ConversationByUserRepositoryImpl(private val reactiveCassandraTemplate: ReactiveCassandraTemplate) :
    ConversationByUserRepository {
    override fun get(userId: String): Flux<ConversationByUser> {
        return reactiveCassandraTemplate.select(
            Query.query(where("user_id").`is`(UUID.fromString(userId))),
            ConversationByUser::class.java
        )
    }

    override fun get(senderId: String, recipientId: String): Mono<ConversationByUser> {
        return reactiveCassandraTemplate.selectOne(
            Query.query(where("user_id").`is`(UUID.fromString(senderId))),
            ConversationByUser::class.java
        )
    }
}