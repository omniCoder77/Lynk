package com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity

import com.lynk.messageservice.domain.model.Room
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("room_by_member")
data class RoomByMember(
    @PrimaryKey
    val roomByMemberKey: RoomByMemberKey,
    val name: String,
) {
    fun toDomain() = Room(
        id = roomByMemberKey.roomId,
        name = name
    )
}

@PrimaryKeyClass
data class RoomByMemberKey(
    @PrimaryKeyColumn(
        value = "member_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED
    ) val memberId: UUID,
    @PrimaryKeyColumn(
        value = "room_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED
    ) val roomId: UUID,
)