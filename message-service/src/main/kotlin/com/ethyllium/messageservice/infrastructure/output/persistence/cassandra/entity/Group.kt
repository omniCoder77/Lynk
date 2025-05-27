package com.ethyllium.messageservice.infrastructure.outpout.persistence.jpa.entity

import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("groups")
data class Group(
    @PrimaryKey val groupId: UUID = UUID.randomUUID(),
    @Column("name") val name: String,
    @Column("description") val description: String?,
    @Column("created_at") val createdAt: Instant = Instant.now(),
    @Column("creator_id") val creatorId: UUID,
)
