package com.lynk.messageservice.domain.port.driven

import com.lynk.messageservice.domain.model.Conversation
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant

interface ConversationService {
    fun getConversations(userId: String,  recipientId: String): Flux<Conversation>
    fun sendMessage(userId: String, recipientId: String, content: String, replyToMessageId: String?, phoneNumber: String): Mono<Boolean>
}