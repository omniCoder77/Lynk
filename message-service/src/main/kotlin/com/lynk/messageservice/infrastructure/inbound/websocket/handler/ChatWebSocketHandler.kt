package com.lynk.messageservice.infrastructure.inbound.websocket.handler

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.lynk.messageservice.domain.port.driven.ConversationService
import com.lynk.messageservice.infrastructure.inbound.websocket.dto.ChatWebsocketMessage
import com.lynk.messageservice.infrastructure.outbound.security.LynkAuthenticationToken
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Component
class ChatWebSocketHandler(private val conversationService: ConversationService) : WebSocketHandler {
    override fun handle(session: WebSocketSession): Mono<Void> {
        return ReactiveSecurityContextHolder.getContext().map { it.authentication as LynkAuthenticationToken }
            .flatMap { authentication ->
                val receive = session.receive().doOnNext { message ->
                    val data = jacksonObjectMapper().readValue(message.payloadAsText, ChatWebsocketMessage::class.java)
                    conversationService.sendMessage(
                        userId = authentication.userId,
                        recipientId = data.recipientId,
                        content = data.content,
                        replyToMessageId = data.replyToMessageId,
                        phoneNumber = authentication.phoneNumber
                    ).subscribeOn(Schedulers.boundedElastic()).subscribe()
                }.then()
                receive
            }
    }
}