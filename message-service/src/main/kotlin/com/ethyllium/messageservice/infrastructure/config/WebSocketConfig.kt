package com.ethyllium.messageservice.infrastructure.config

import com.ethyllium.messageservice.infrastructure.adapter.inbound.websocket.UserHandshakeHandler
import com.ethyllium.messageservice.infrastructure.adapter.inbound.websocket.UserHandshakeInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

/**
AVAILABLE TOPICS (for client subscriptions):

Public Chat Rooms:
- /topic/chat.general - General chat room
- /topic/chat.{roomId} - Specific chat rooms
- /topic/chat.{roomId}.typing - Typing indicators for rooms

Presence & Status:
- /topic/presence.online - Online users list
- /topic/presence.{roomId} - Users in specific room

File Sharing:
- /topic/files.{roomId} - File upload notifications in rooms

Announcements:
- /topic/announcements - Server-wide announcements
- /topic/room.{roomId}.announcements - Room-specific announcements

Private Messages:
- /queue/direct.{userId} - Direct messages queue

User-Specific Notifications:
- /user/{userId}/queue/notifications - Personal notifications
- /user/{userId}/queue/direct - Direct messages
- /user/{userId}/queue/mentions - Message mentions
- /user/{userId}/queue/friend-requests - Friend request notifications

APPLICATION DESTINATIONS (for client sending):

Chat Messages:
- /app/chat.send - Send message to room
- /app/chat.join - Join chat room
- /app/chat.leave - Leave chat room
- /app/chat.history - Request chat history

Direct Messages:
- /app/direct.send - Send direct message
- /app/direct.history - Request DM history

Typing Indicators:
- /app/typing.start - Start typing in room
- /app/typing.stop - Stop typing in room

Presence:
- /app/presence.update - Update online status
- /app/presence.join-room - Join room presence
- /app/presence.leave-room - Leave room presence

Message Management:
- /app/message.edit - Edit sent message
- /app/message.delete - Delete message
- /app/message.react - Add reaction to message

User Management:
- /app/user.block - Block user
- /app/user.unblock - Unblock user
- /app/friend.request - Send friend request
- /app/friend.accept - Accept friend request
- /app/friend.reject - Reject friend request

Room Management:
- /app/room.create - Create new room
- /app/room.update - Update room settings
- /app/room.delete - Delete room
- /app/room.invite - Invite user to room
- /app/room.kick - Kick user from room (admin)

File Operations:
- /app/file.upload - Notify file upload
- /app/file.share - Share file in room/DM

Admin Operations:
- /app/admin.ban - Ban user (admin only)
- /app/admin.unban - Unban user (admin only)
- /app/admin.mute - Mute user in room (moderator)
- /app/admin.unmute - Unmute user (moderator)
- /app/admin.announce - Send server announcement
 */
@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(
    private val userHandshakeInterceptor: UserHandshakeInterceptor,
    private val userHandshakeHandler: UserHandshakeHandler
) :
    WebSocketMessageBrokerConfigurer {

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws/chat").setAllowedOriginPatterns("*").addInterceptors(userHandshakeInterceptor).setHandshakeHandler(userHandshakeHandler)
            .withSockJS()

        registry.addEndpoint("/ws/files").setAllowedOriginPatterns("*").addInterceptors(userHandshakeInterceptor).setHandshakeHandler(userHandshakeHandler)
            .withSockJS()

        registry.addEndpoint("/ws/admin").setAllowedOriginPatterns("*").addInterceptors(userHandshakeInterceptor).setHandshakeHandler(userHandshakeHandler)
            .withSockJS()
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker(
            "/topic", "/queue", "/user"
        )
        registry.setApplicationDestinationPrefixes("/app")
        registry.setUserDestinationPrefix("/user")
    }
}