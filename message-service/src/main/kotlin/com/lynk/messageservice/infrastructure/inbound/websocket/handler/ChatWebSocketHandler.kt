package com.lynk.messageservice.infrastructure.inbound.websocket.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.lynk.messageservice.domain.port.driven.ConversationService
import com.lynk.messageservice.domain.port.driven.OnlineTrackerService
import com.lynk.messageservice.infrastructure.inbound.websocket.dto.ChatWebsocketMessage
import com.lynk.messageservice.infrastructure.outbound.security.LynkAuthenticationToken
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Component
class ChatWebSocketHandler(
    private val mapper: ObjectMapper,
    private val conversationService: ConversationService,
    private val onlineService: OnlineTrackerService
) : WebSocketHandler {

    override fun handle(session: WebSocketSession): Mono<Void> {
        return ReactiveSecurityContextHolder.getContext().map { it.authentication as LynkAuthenticationToken }
            .flatMap { auth ->
                val userId = auth.userId

                onlineService.setUserOnline(userId)
                    .then(session.receive().publishOn(Schedulers.boundedElastic()).flatMap { msg ->
                            val data = mapper.readValue(msg.payloadAsText, ChatWebsocketMessage::class.java)
                            conversationService.sendMessage(
                                userId = auth.userId,
                                recipientId = data.recipientId,
                                content = data.content,
                                replyToMessageId = data.replyToMessageId,
                                phoneNumber = auth.phoneNumber
                            )
                        }.onErrorResume { Mono.empty() }.then()).doFinally {
                        onlineService.setUserOffline(userId).subscribe()
                    }
            }
    }
}