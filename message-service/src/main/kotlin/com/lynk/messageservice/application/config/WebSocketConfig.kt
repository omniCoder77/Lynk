package com.lynk.messageservice.application.config

import com.lynk.messageservice.infrastructure.inbound.web.websocket.ChatWebSocketHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter

@Configuration
class WebSocketConfig {

    @Bean
    fun handlerMapping(webSocketHandler: ChatWebSocketHandler): HandlerMapping {
        val map = mapOf("/ws/chat" to webSocketHandler)
        return SimpleUrlHandlerMapping(map, -1)
    }

    @Bean
    fun handlerAdapter(): WebSocketHandlerAdapter {
        return WebSocketHandlerAdapter()
    }
}