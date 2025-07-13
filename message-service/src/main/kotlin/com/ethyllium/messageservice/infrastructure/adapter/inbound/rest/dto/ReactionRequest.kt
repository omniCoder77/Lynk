package com.ethyllium.messageservice.infrastructure.adapter.inbound.rest.dto

data class ReactionRequest (
    val action: ReactionAction,
    val emoji: String,
    val messageId: String
)

enum class ReactionAction {
    ADD, REMOVE
}