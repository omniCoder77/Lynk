package com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra

import com.ethyllium.messageservice.domain.port.outbound.UserRepository
import com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.entity.UserEntity
import org.springframework.data.cassandra.core.ReactiveCassandraOperations
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class UserRepositoryImpl(
    private val reactiveCassandraOperations: ReactiveCassandraOperations
) : UserRepository {
    override fun insertUser(userId: String): Mono<String> {
        return reactiveCassandraOperations.insert(UserEntity(userId)).map { it.id }
    }
}