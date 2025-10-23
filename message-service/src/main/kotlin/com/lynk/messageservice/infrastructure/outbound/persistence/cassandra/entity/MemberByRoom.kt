package com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity

import com.lynk.messageservice.domain.model.RoomMember
import com.lynk.messageservice.domain.model.RoomRole
import com.lynk.messageservice.infrastructure.util.BucketUtils.bucket
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import java.io.Serializable
import java.time.Instant
import java.util.*

@Table("member_by_room")
data class MemberByRoom(
    @PrimaryKey val memberByRoomKey: MemberByRoomKey,
    @Column("description")
    val description: String? = null,
    @Column("display_name")
    val displayName: String,
    @Column("role")
    val role: String,
    @Column("joined_at")
    val joinedAt: Instant,
): Serializable {
    fun toDomain() = RoomMember(
        memberId = memberByRoomKey.memberId,
        roomId = memberByRoomKey.roomId,
        joinedAt = joinedAt,
        description = description,
        displayName = displayName,
        role = RoomRole.valueOf(role)
    )
}

@PrimaryKeyClass
data class MemberByRoomKey(
    @PrimaryKeyColumn(value = "room_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    val roomId: UUID,
    @PrimaryKeyColumn(value = "bucket", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    val bucket: Int = roomId.bucket(),
    @PrimaryKeyColumn(ordinal = 0, value = "member_id", type = PrimaryKeyType.CLUSTERED)
    val memberId: UUID
): Serializable