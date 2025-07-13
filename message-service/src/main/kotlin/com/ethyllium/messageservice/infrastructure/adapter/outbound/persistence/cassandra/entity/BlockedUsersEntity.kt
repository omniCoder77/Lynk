package com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.entity

import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.*
import java.time.Instant

@Table("blocked_users")
data class BlockedUserEntity(
    @PrimaryKey val blockedUserKey: BlockedUserKey,

    @Column("blocked_at") val blockedAt: Instant = Instant.now()
)

@PrimaryKeyClass
data class BlockedUserKey(
    @PrimaryKeyColumn(name = "user_id", type = PrimaryKeyType.PARTITIONED) val userId: String,

    @PrimaryKeyColumn(name = "blocked_user_id", type = PrimaryKeyType.CLUSTERED) val blockedUserId: String
)