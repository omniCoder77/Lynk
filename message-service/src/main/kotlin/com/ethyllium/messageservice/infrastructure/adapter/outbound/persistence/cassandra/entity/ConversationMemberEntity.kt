package com.ethyllium.messageservice.infrastructure.adapter.outbound.persistence.cassandra.entity

import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import java.time.Instant

@Table("conversation_members")
data class ConversationMemberEntity(
    @PrimaryKey val key: ConversationMemberKey,
    @Column("is_admin") val isAdmin: Boolean = false,
    @Column("joined_at") val joinedAt: Instant = Instant.now()
)

@PrimaryKeyClass
data class ConversationMemberKey(
    @PrimaryKeyColumn(name = "conversation_id", type = PrimaryKeyType.PARTITIONED) val conversationId: String,
    @PrimaryKeyColumn(name = "user_id", type = PrimaryKeyType.CLUSTERED) val userId: String
)