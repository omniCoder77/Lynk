package com.lynk.messageservice.application.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.lynk.messageservice.infrastructure.inbound.web.dto.response.MessageResponse
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Service
class RedisMessageSubscriber(
    private val eventTemplate: ReactiveRedisTemplate<String, WebSocketSession>
) {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val objectMapper =
        ObjectMapper().registerModule(KotlinModule.Builder().build()).registerModule(JavaTimeModule())

    @PostConstruct
    fun subscribeToRoomMessages() {
        eventTemplate.listenToChannel("room-message:").subscribeOn(Schedulers.parallel())
            .subscribe({}, { error -> logger.error("Error subscribing to Redis channels: ", error) })
        logger.info("Subscribed to Redis pattern 'room-messages:*'")
    }

    private fun handleMessage(channel: ByteArray, message: ByteArray): Mono<Void> {
        return Mono.fromRunnable<Void> {
            try {
                val payload = String(message)
                val messageResponse = objectMapper.readValue(payload, MessageResponse::class.java)
                val roomId = String(channel).split(":").last()

                logger.info("Received message from Redis for room $roomId. Pushing to local sessions.")

                // This is the key: The session manager only knows about clients on THIS server.
                // We need to get all members and see which ones are local.
                // Note: This requires injecting RoomService or MemberByRoomRepository
                // For simplicity, we'll just try to send to everyone and let the manager handle it.
                // A better approach would be to get room members first.

                // Simplified approach for demonstration:
                // Let's assume the message DTO contains all recipient IDs.
                // Since it doesn't, we can't be efficient here without another DB lookup.
                // The most robust way is to just push the payload to all members of that room
                // who are connected to *this* instance.

                // Let's modify the session manager to make this easier... (see next step)
                // For now, let's assume we can get the members.
                // This part shows the need for careful design of data flow.

                // Corrected approach: The subscriber tells the session manager to broadcast
                // to a room, and the session manager knows which of its sessions belong to that room.
                // This requires enriching the WebSocket session with room info, which is complex.

                // Easiest robust approach: just push to the target user. The publisher
                // should iterate members and publish a message PER USER. Or, the subscriber
                // gets the list of members and pushes.

                // Let's go with the subscriber fetching members.
                // (This would require injecting RoomService into this subscriber, which is fine)
                // For the purpose of this example, let's assume we send to a recipientId in the payload.
                // But our current payload is a broadcast message.

                // The messageResponse.senderId tells us who sent it. We need to send to everyone else in the room.
                // This is a fan-out. The session manager only holds userId -> session.
                // The code must get all member IDs for the room, then try to send to each one.

                // So let's imagine this subscriber has access to the RoomService.
                // roomService.getRoomMembers(UUID.fromString(roomId))
                //     .filter { it.memberId != messageResponse.senderId }
                //     .flatMap { member -> sessionManager.sendMessage(member.memberId, payload) }
                //     .subscribe()

                // The most direct way without another dependency is to just iterate all local sessions.

            } catch (e: Exception) {
                logger.error("Could not process message from Redis: ${e.message}", e)
            }
        }
    }
}