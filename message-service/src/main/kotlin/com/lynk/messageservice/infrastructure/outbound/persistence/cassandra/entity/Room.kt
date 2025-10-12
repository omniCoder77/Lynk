package com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity

import com.lynk.messageservice.domain.model.RoomType
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("room")
data class Room(
    @PrimaryKey("room_id") val roomId: UUID = UUID.randomUUID(),
    val name: String,
    val creatorId: UUID,
    val createdAt: Instant = Instant.now(),
    val lastActivityTimestamp: Instant = createdAt,
    val roomType: String = RoomType.PUBLIC.name,
    val avatarUrl: String? = null,
    val description: String? = null
)