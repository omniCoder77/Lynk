package com.ethyllium.messageservice.infrastructure.adapter.inbound.websocket

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectedEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent


@Component
class WebSocketEventListener {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @EventListener
    fun handleSessionConnected(event: SessionConnectedEvent) {
        val userId = event.user?.name
        logger.info(userId)
    }

    @EventListener
    fun handleWebSocketDisconnectListener(event: SessionDisconnectEvent) {
        val headerAccessor = StompHeaderAccessor.wrap(event.message)
        val username = headerAccessor.sessionAttributes!!.get("username") as String?
        logger.info(username)
    }
}