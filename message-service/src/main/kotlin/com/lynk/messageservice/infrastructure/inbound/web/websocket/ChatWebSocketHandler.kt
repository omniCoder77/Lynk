package com.lynk.messageservice.infrastructure.inbound.web.websocket

import com.lynk.messageservice.application.service.PresenceService
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import java.util.*

@Component
class ChatWebSocketHandler(
    private val presenceService: PresenceService
) : WebSocketHandler {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun handle(session: WebSocketSession): Mono<Void> {
        return ReactiveSecurityContextHolder.getContext()
            .map { it.authentication }
            .flatMap { authentication ->
                val userId = UUID.fromString(authentication.name)
                logger.info("WebSocket connection established for user: $userId [Session ID: ${session.id}]")

                presenceService.setUserOnline(userId)
                    .then(handleSession(session, userId))
            }
            .switchIfEmpty(
                Mono.defer {
                    logger.warn("WebSocket connection rejected: No authenticated user found.")
                    session.close()
                }
            )
    }

    private fun handleSession(session: WebSocketSession, userId: UUID): Mono<Void> {
        val input = session.receive()
            .doOnNext { message: WebSocketMessage ->
                logger.info("Received from user $userId: ${message.payloadAsText}")
            }
            .then()

        val connectionClosed = session.closeStatus()
            .doOnSuccess {
                logger.info("WebSocket connection closed for user: $userId [Session ID: ${session.id}]")
                presenceService.setUserOffline(userId).subscribe()
            }
            .then()

        return Mono.firstWithSignal(input, connectionClosed)
    }
}