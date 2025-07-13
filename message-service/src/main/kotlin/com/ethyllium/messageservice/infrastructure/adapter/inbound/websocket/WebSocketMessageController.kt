package com.ethyllium.messageservice.infrastructure.adapter.inbound.websocket

import com.ethyllium.messageservice.infrastructure.adapter.inbound.rest.dto.ReactionRequest
import com.ethyllium.messageservice.infrastructure.adapter.inbound.websocket.dto.ReceiverMessage
import com.ethyllium.messageservice.infrastructure.adapter.inbound.websocket.dto.SenderMessage
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller

@Controller
class WebSocketMessageController(
    private val simpMessagingTemplate: SimpMessagingTemplate
) {

    @MessageMapping("/chat.send")
    fun sendChatMessage(
        @Payload message: SenderMessage, @Header("simpSessionAttributes") attributes: Map<String, Any>
    ) {
        val userId = attributes["userId"] as String
        val receiverMessage = ReceiverMessage(message.content, userId)
        message.recipient.forEach {
            simpMessagingTemplate.convertAndSendToUser(it, "/queue/private", receiverMessage)
        }
    }

    @MessageMapping("/chat.join")
    fun joinChatRoom(@Payload joinRequest: String) {
        // logic to join room
    }

    @MessageMapping("/chat.leave")
    fun leaveChatRoom(@Payload leaveRequest: String) {
        // logic to leave room
    }

    @MessageMapping("/chat.history")
    fun requestChatHistory(@Payload historyRequest: String) {
        // logic to retrieve chat history
    }

    @MessageMapping("/direct.send")
    fun sendDirectMessage(@Payload message: String) {
        // send to /queue/direct.{userId}
    }

    @MessageMapping("/direct.history")
    fun requestDMHistory(@Payload historyRequest: String) {
        // logic to retrieve DM history
    }

    @MessageMapping("/typing.start")
    fun startTyping(@Payload typingPayload: String) {
    }

    @MessageMapping("/typing.stop")
    fun stopTyping(@Payload typingPayload: String) {
    }

    @MessageMapping("/presence.update")
    fun updatePresence(@Payload presenceUpdate: String) {
    }

    @MessageMapping("/presence.join-room")
    fun joinRoomPresence(@Payload payload: String) {
    }

    @MessageMapping("/presence.leave-room")
    fun leaveRoomPresence(@Payload payload: String) {
    }

    @MessageMapping("/message.edit")
    fun editMessage(@Payload payload: String) {
    }

    @MessageMapping("/message.delete")
    fun deleteMessage(@Payload payload: String) {
    }

    @MessageMapping("/message.react")
    fun reactToMessage(@Payload request: ReactionRequest) {
        // pass to MessageInteractionService
    }

    @MessageMapping("/user.block")
    fun blockUser(@Payload payload: String) {
    }

    @MessageMapping("/user.unblock")
    fun unblockUser(@Payload payload: String) {
    }

    @MessageMapping("/friend.request")
    fun sendFriendRequest(@Payload payload: String) {
    }

    @MessageMapping("/friend.accept")
    fun acceptFriendRequest(@Payload payload: String) {
    }

    @MessageMapping("/friend.reject")
    fun rejectFriendRequest(@Payload payload: String) {
    }

    @MessageMapping("/room.create")
    fun createRoom(@Payload payload: String) {
    }

    @MessageMapping("/room.update")
    fun updateRoom(@Payload payload: String) {
    }

    @MessageMapping("/room.delete")
    fun deleteRoom(@Payload payload: String) {
    }

    @MessageMapping("/room.invite")
    fun inviteToRoom(@Payload payload: String) {
    }

    @MessageMapping("/room.kick")
    fun kickFromRoom(@Payload payload: String) {
    }

    @MessageMapping("/file.upload")
    fun notifyFileUpload(@Payload payload: String) {
    }

    @MessageMapping("/file.share")
    fun shareFile(@Payload payload: String) {
    }

    @MessageMapping("/admin.ban")
    fun banUser(@Payload payload: String) {
    }

    @MessageMapping("/admin.unban")
    fun unbanUser(@Payload payload: String) {
    }

    @MessageMapping("/admin.mute")
    fun muteUser(@Payload payload: String) {
    }

    @MessageMapping("/admin.unmute")
    fun unmuteUser(@Payload payload: String) {
    }

    @MessageMapping("/admin.announce")
    fun announce(@Payload payload: String) {
    }
}