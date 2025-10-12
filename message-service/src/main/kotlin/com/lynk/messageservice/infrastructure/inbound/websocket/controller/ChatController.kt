package com.lynk.messageservice.infrastructure.inbound.websocket.controller

import com.lynk.messageservice.domain.port.driven.ChatService
import com.lynk.messageservice.infrastructure.inbound.websocket.dto.Conversation
import com.lynk.messageservice.infrastructure.inbound.websocket.dto.Room
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import reactor.core.publisher.Mono

@Controller
class ChatController(private val simpMessagingTemplate: SimpMessagingTemplate, private val chatService: ChatService) {
    @MessageMapping("/conversation")
    fun conversation(conversation: Conversation, authentication: Authentication): Mono<String> {
        return chatService.store(conversation.message, conversation.recipientPhoneNumber, authentication.name).map {
            if (it) {
                simpMessagingTemplate.convertAndSend("/topic/greet/${conversation.recipientPhoneNumber}", conversation)
                "ACK"
            } else
                "NACK"
        }
    }

    @MessageMapping("/room")
    fun room(room: Room) {

    }
}