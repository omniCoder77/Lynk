package com.lynk.messageservice.domain.port.driven

import com.lynk.messageservice.domain.model.Conversation
import reactor.core.publisher.Flux
import java.time.Instant

interface ConversationService {
    fun getConversations(userId: String, start: Instant, end: Instant): Flux<Conversation>
}