package com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra

import com.ethyllium.messageservice.domain.port.outbound.UserRepository
import com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.entity.UserEntity
import com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.repository.CassandraUserEntityRepository
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class UserRepositoryImpl(private val cassandraUserEntityRepository: CassandraUserEntityRepository) : UserRepository {
    override fun insertUser(userId: String): Mono<String> {
        return cassandraUserEntityRepository.insert(UserEntity(userId)).map { it.id }
    }
}