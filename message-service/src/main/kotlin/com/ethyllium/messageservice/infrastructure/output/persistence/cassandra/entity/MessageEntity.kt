package com.ethyllium.messageservice.infrastructure.outpout.persistence.jpa.entity

import org.springframework.data.cassandra.core.cql.Ordering
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("messages")
data class MessageEntity(

    @PrimaryKeyColumn(name = "conversation_id", type = PrimaryKeyType.PARTITIONED) val conversationId: UUID,

    @PrimaryKeyColumn(
        name = "message_id",
        type = PrimaryKeyType.CLUSTERED,
        ordering = Ordering.ASCENDING
    ) val messageId: UUID = UUID.randomUUID(),

    @Column("sender_id") val senderId: UUID,

    @Column("receiver_id") val receiverId: UUID,

    @Column("content") val content: String,

    @Column("timestamp") val timestamp: Instant = Instant.now(),

    @Column("status")
    val status: String = "SENT"
)
