package com.ethyllium.messageservice.infrastructure.adapter.inbound.websocket

import org.springframework.http.server.ServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.support.DefaultHandshakeHandler
import java.security.Principal

@Component
class UserHandshakeHandler : DefaultHandshakeHandler() {
    override fun determineUser(
        request: ServerHttpRequest, wsHandler: WebSocketHandler, attributes: Map<String?, Any?>
    ): Principal? {
        val userId = attributes["userId"] as? String? ?: return null

        return Principal {
            userId
        }
    }
}