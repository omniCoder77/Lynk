package com.ethyllium.messageservice.infrastructure.adapter.inbound.websocket

import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor

@Component
class UserHandshakeInterceptor : HandshakeInterceptor {

    private fun parseQueryString(query: String): Map<String, String> {
        return query.split("&").mapNotNull { param ->
            val parts = param.split("=", limit = 2)
            if (parts.size == 2) {
                parts[0] to parts[1]
            } else null
        }.toMap()
    }

    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: Map<String?, Any?>
    ): Boolean {
        val query = request.uri.query

        if (query != null) {
            val params = parseQueryString(query)
            val userId = params["userId"]
            val to = params["to"]
            if (userId.isNullOrBlank() || to.isNullOrBlank()) {
                return false
            }
            val type = params["type"] as String
            if ((type != "private") && (type != "room")) return false
            (attributes as HashMap<String, Any>)["userId"] = userId
            attributes["to"] = to
            attributes["type"] = type
        }
        return true

    }

    override fun afterHandshake(
        request: ServerHttpRequest, response: ServerHttpResponse, wsHandler: WebSocketHandler, exception: Exception?
    ) {
    }
}