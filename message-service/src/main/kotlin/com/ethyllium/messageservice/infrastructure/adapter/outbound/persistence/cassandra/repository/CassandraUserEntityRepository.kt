package com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.repository

import com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.entity.UserEntity
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository
import org.springframework.stereotype.Repository

@Repository
interface CassandraUserEntityRepository: ReactiveCassandraRepository<UserEntity, String> {
}