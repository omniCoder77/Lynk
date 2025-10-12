package com.lynk.messageservice.domain.port.driven

import reactor.core.publisher.Mono

interface ChatService {
    fun store(message: String, recipientId: String, senderId: String): Mono<Boolean>
}