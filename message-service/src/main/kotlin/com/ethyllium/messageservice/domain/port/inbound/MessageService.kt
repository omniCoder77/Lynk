package com.ethyllium.messageservice.domain.port.inbound

import com.ethyllium.messageservice.application.dto.MessageRequest
import com.ethyllium.messageservice.domain.model.Message
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import reactor.core.publisher.Mono

interface MessageService {
    fun sendMessage(senderId: String, request: MessageRequest): Mono<Pair<String, Long>>
    fun editMessage(userId: String, messageId: String, content: String, createdAt: Long): Mono<Boolean>
    fun deleteMessage(userId: String, messageId: String, createdAt: Long): Mono<Boolean>
    fun getUserMessages(
        userId: String, conversationId: String?, days: Int, pageable: Pageable
    ): Page<Message>
}