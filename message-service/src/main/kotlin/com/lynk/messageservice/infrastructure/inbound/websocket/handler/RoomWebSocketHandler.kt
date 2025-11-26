package com.lynk.messageservice.infrastructure.inbound.websocket.handler

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.lynk.messageservice.domain.port.driven.RoomService
import com.lynk.messageservice.infrastructure.inbound.websocket.dto.RoomWebsocketMessage
import com.lynk.messageservice.infrastructure.outbound.security.LynkAuthenticationToken
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.*

@Component
class RoomWebSocketHandler(private val roomService: RoomService) : WebSocketHandler {

    override fun handle(session: WebSocketSession): Mono<Void> {
        return ReactiveSecurityContextHolder.getContext().map { it.authentication as LynkAuthenticationToken }
            .flatMap { authentication ->
                val receive = session.receive().doOnNext { message ->
                    val data = jacksonObjectMapper().readValue(message.payloadAsText, RoomWebsocketMessage::class.java)
                    val roomId = UUID.fromString(data.roomId)
                    val userId = UUID.fromString(authentication.userId)
                    val replyToMessageId = data.replyToMessageId?.let { UUID.fromString(it) }
                    roomService.sendMessage(
                        roomId = roomId,
                        senderId = userId,
                        content = data.content,
                        replyToMessageId = replyToMessageId,
                        timestamp = data.timestamp,
                        phoneNumber = authentication.phoneNumber
                    ).subscribeOn(Schedulers.boundedElastic()).subscribe()
                }.then()
                receive
            }
    }
}