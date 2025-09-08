package com.lynk.messageservice.infrastructure.inbound.websocket

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller


@Controller
class ChatController(private val simpMessagingTemplate: SimpMessagingTemplate) {
    @MessageMapping("/chat")
    @SendTo("/topic/greetings")
    fun greet(message: String): String {
        return "Hello, " + message + "!"
    }
}