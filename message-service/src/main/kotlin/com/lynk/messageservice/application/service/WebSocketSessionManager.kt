package com.lynk.messageservice.application.service

import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Service
class WebSocketSessionManager {

    private val sessions = ConcurrentHashMap<UUID, WebSocketSession>()

    fun register(userId: UUID, session: WebSocketSession) {
        sessions[userId] = session
    }

    fun unregister(userId: UUID) {
        sessions.remove(userId)
    }

    fun getSession(userId: UUID): WebSocketSession? {
        return sessions[userId]
    }

    fun sendMessage(userId: UUID, payload: String): Mono<Void> {
        val session = getSession(userId)
        return if (session != null && session.isOpen) {
            val message = session.textMessage(payload)
            session.send(Mono.just(message))
        } else {
            Mono.empty()
        }
    }
}