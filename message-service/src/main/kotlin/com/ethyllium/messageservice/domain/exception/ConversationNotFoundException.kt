package com.ethyllium.messageservice.domain.exception

class ConversationNotFoundException(conversationId: String) : RuntimeException("Conversation with ID $conversationId not found")