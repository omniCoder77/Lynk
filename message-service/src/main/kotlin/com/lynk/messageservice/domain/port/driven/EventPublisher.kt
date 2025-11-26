package com.lynk.messageservice.domain.port.driven

import com.lynk.messageservice.infrastructure.outbound.kafka.dto.ConversationMessageEvent
import com.lynk.messageservice.infrastructure.outbound.kafka.dto.RoomMessageEvent

interface EventPublisher {
    fun publish(request: ConversationMessageEvent)
    fun publish(request: RoomMessageEvent)
}