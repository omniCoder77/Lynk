package com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.repository

import com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.entity.BlockedUserEntity
import com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.entity.BlockedUserKey
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data interface for basic CRUD operations on the 'blocked_users' table.
 * Spring will automatically provide the implementation for this at runtime.
 */
@Repository
interface CassandraBlockedUserRepository : ReactiveCassandraRepository<BlockedUserEntity, BlockedUserKey> {
}