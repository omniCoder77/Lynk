package com.lynk.messageservice.infrastructure.inbound.web.controller

import com.lynk.messageservice.domain.port.driven.ConversationService
import com.lynk.messageservice.infrastructure.outbound.persistence.cassandra.entity.ConversationByUser
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/conversations")
class ConversationController(private val conversationService: ConversationService) {

    @GetMapping
    fun getConversations(authentication: Authentication): Flux<ConversationByUser> {
        val userId = authentication.principal as User
        return conversationService.getConversations(userId.username)
    }

    @GetMapping("/{recipientId}")
    fun getConversationById(@PathVariable recipientId: String, authentication: Authentication): Mono<ConversationByUser> {
        val userId = authentication.principal as String
        return conversationService.getConversation(userId, recipientId)
    }
}