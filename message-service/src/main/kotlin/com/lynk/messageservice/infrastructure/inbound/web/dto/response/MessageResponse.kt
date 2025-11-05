package com.lynk.messageservice.infrastructure.inbound.web.dto.response

import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.RoomMessage
import java.time.Instant
import java.util.UUID

data class MessageResponse(
    val messageId: UUID,
    val roomId: UUID,
    val senderId: UUID,
    val content: String,
    val timestamp: Instant,
    val replyToMessageId: UUID?,
    val reactions: Map<String, Int>
)
fun RoomMessage.toResponse(): MessageResponse {
    return MessageResponse(
        messageId = this.key.messageId,
        roomId = this.key.roomId,
        senderId = this.sender_id,
        content = this.content,
        timestamp = this.key.timestamp,
        replyToMessageId = this.reply_to_message_id,
        reactions = this.reactions
    )
}