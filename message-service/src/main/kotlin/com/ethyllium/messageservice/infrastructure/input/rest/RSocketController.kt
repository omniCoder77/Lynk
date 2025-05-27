package com.ethyllium.messageservice.infrastructure.input.resp

import com.ethyllium.messageservice.application.service.MessageService
import com.ethyllium.messageservice.domain.model.Message
import com.ethyllium.messageservice.infrastructure.input.resp.dto.request.MessageRequest
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers

@Controller
class RSocketController(private val messageService: MessageService) {

    @MessageMapping("message.create")
    suspend fun createMessage(request: MessageRequest): Message {
        return messageService.createMessage(request.content, request.sender)
    }

    @MessageMapping("message.get")
    suspend fun getMessage(id: String): Message? {
        return messageService.getMessage(id)
    }

    @MessageMapping("message.stream")
    fun streamMessages(): Flux<Message> {
        return messageService.getMessageStream()
            .subscribeOn(Schedulers.boundedElastic()) // Optimize for I/O-bound operations
            .onBackpressureBuffer(1000) // Handle backpressure with buffer
    }
}
